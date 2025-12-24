package com.farmatodo.challenge.application.service;
import com.farmatodo.challenge.domain.model.Cart;
import com.farmatodo.challenge.domain.model.*;
import com.farmatodo.challenge.domain.port.in.OrderUseCase;
import com.farmatodo.challenge.domain.port.out.*;
import com.farmatodo.challenge.infrastructure.persistence.entity.CustomerEntity;
import com.farmatodo.challenge.infrastructure.persistence.entity.OrderEntity;
import com.farmatodo.challenge.infrastructure.persistence.repository.CardTokenJpaRepository;
import com.farmatodo.challenge.infrastructure.persistence.repository.CustomerJpaRepository;
import com.farmatodo.challenge.infrastructure.persistence.repository.OrderJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService implements OrderUseCase {

    private final OrderJpaRepository orderRepository;
    private final InventoryPort inventoryPort;
    private final NotificationPort notificationPort;
    private final TransactionLogPort transactionLogPort;
    private final CartRepositoryPort cartRepository;
    // Inyectamos el repo de clientes para buscar los datos
    private final CustomerJpaRepository customerRepository;
    private final CardTokenJpaRepository  cardTokenRepository;
    @Override
    @Transactional
    public Order checkoutCart(String customerEmail, UUID cardTokenId) {

        boolean isTokenExpired = cardTokenRepository.findById(cardTokenId).get().getExpirationDate().isBefore(LocalDateTime.now());

        if(isTokenExpired) {
            throw new IllegalArgumentException("El token de la tarjeta esta expirado");
        }

        // 1. Buscar el carrito del usuario
        Cart cart = cartRepository.findByCustomerEmail(customerEmail)
                .orElseThrow(() -> new IllegalArgumentException("No hay carrito activo para: " + customerEmail));

        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("El carrito está vacío.");
        }

        // 2. Convertir Items de Carrito -> Items de Orden
        List<OrderItem> orderItems = new ArrayList<>();
        for (com.farmatodo.challenge.domain.model.CartItem cartItem : cart.getItems()) {
            orderItems.add(new OrderItem(
                    cartItem.getProductId(),
                    "Pending",
                    java.math.BigDecimal.ZERO,
                    cartItem.getQuantity()
            ));
        }

        // 3. Crear el objeto Orden preliminar
        Order orderInput = Order.builder()
                .customer(com.farmatodo.challenge.domain.model.Customer.builder().email(customerEmail).build())
                .cardTokenId(cardTokenId)
                .items(orderItems)
                .build();

        // 4. Procesar la orden (Reutilizamos la lógica robusta de processOrder)
        Order completedOrder = this.processOrder(orderInput);

        // Pago exitoso: Borramos el carrito.
        cartRepository.deleteByCustomerEmail(customerEmail);
        log.info("Carrito eliminado tras compra exitosa para: {}", customerEmail);

        return completedOrder;
    }

    @Value("${app.business.payment.max-retries:3}")
    private int maxRetries;

    @Value("${app.business.payment.rejection-probability:0.2}")
    private double paymentRejectionProbability;


    public Map<String, Object> getCurrentConfig() {
        return Map.of(
                "paymentRejectionProbability", paymentRejectionProbability,
                "maxRetries", maxRetries
        );
    }

    public void setPaymentRejectionProbability(double probability) {
        if (probability < 0 || probability > 1) {
            throw new IllegalArgumentException("La probabilidad debe estar entre 0.0 y 1.0");
        }
        this.paymentRejectionProbability = probability;
        log.info("ADMIN: Probabilidad de rechazo actualizada a: {}", probability);
    }

    public int getMaxRetries() { return maxRetries; }

    public double getPaymentRejectionProbability() { return paymentRejectionProbability; }

    public void setMaxRetries(int retries) {
        this.maxRetries = retries;
        log.info("ADMIN: Max reintentos actualizados a: {}", retries);
    }



    @Override
    @Transactional
    public Order processOrder(Order orderInput) {

        // Buscamos el cliente real en la DB usando el email
        CustomerEntity customerEntity = customerRepository.findByEmail(orderInput.getCustomer().getEmail())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con email: " + orderInput.getCustomer().getEmail()));

        // Mapeamos la entidad completa al dominio
        Customer fullCustomer = Customer.builder()
                .id(customerEntity.getId())
                .name(customerEntity.getName())
                .email(customerEntity.getEmail())
                .phoneNumber(customerEntity.getPhoneNumber())
                .address(customerEntity.getAddress())
                .build();
        // Buscamos precio y nombre real del producto.
        List<OrderItem> enrichedItems = new ArrayList<>();
        for (OrderItem item : orderInput.getItems()) {
            Product product = inventoryPort.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado ID: " + item.getProductId()));

            // Creamos un item nuevo con el nombre y precio de la BD
            enrichedItems.add(new OrderItem(
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    item.getQuantity()
            ));
        }

        // 3. Construimos la Orden final con los datos completos
        Order order = Order.builder()
                .transactionId(UUID.randomUUID())
                .customer(fullCustomer)
                .items(enrichedItems)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .cardTokenId(orderInput.getCardTokenId())
                .build();

        // Calculamos el total
        order.calculateTotal();

        UUID txId = order.getTransactionId();
        transactionLogPort.logEvent(txId, "ORDER_INIT", "PENDING", "Iniciando orden para " + fullCustomer.getName());

        // 4. Reserva de Stock
        order.getItems().forEach(item -> {
            boolean reserved = inventoryPort.decreaseStock(item.getProductId(), item.getQuantity());
            if (!reserved) {
                String msg = "Stock insuficiente para: " + item.getProductName();
                transactionLogPort.logEvent(txId, "STOCK_RESERVATION", "FAILURE", msg);
                throw new RuntimeException(msg);
            }
        });
        transactionLogPort.logEvent(txId, "STOCK_RESERVATION", "SUCCESS", "Stock reservado");
        // 5. Proceso de Pago (Simulado)
        boolean approved = false;
        int attempts = 0;

        while (attempts < maxRetries && !approved) {
            attempts++;
            approved = simulatePaymentGateway();
            transactionLogPort.logEvent(txId, "PAYMENT_ATTEMPT", approved ? "SUCCESS" : "FAILURE", "Intento #" + attempts);
        }
        order.setPaymentAttempts(attempts);
        // 6. Finalización
        if (approved) {
            order.setStatus(OrderStatus.APPROVED);
            saveOrder(order); // Guardamos
            notificationPort.sendEmail(order.getCustomer().getEmail(), "Compra Exitosa",
                    "Hola " + fullCustomer.getName() + ", tu pedido por $" + order.getTotalAmount() + " fue aprobado.");
            transactionLogPort.logEvent(txId, "ORDER_FINALIZATION", "APPROVED", "Total: " + order.getTotalAmount());
        } else {

            notificationPort.sendEmail(order.getCustomer().getEmail(), "Pago Fallido",
                    "Hola " + fullCustomer.getName() + ", lamentablemente tu pago fue rechazado. Por favor intenta nuevamente.");

            transactionLogPort.logEvent(txId, "ORDER_FINALIZATION", "REJECTED", "Pago rechazado. Revirtiendo stock.");
            throw new RuntimeException("Pago rechazado tras " + maxRetries + " intentos.");
        }

        return order;
    }

    private boolean simulatePaymentGateway() {
        return ThreadLocalRandom.current().nextDouble() > paymentRejectionProbability;
    }

    private void saveOrder(Order order) {
        OrderEntity entity = new OrderEntity();
        entity.setId(order.getTransactionId());
        entity.setCustomerEmail(order.getCustomer().getEmail());
        entity.setTotalAmount(order.getTotalAmount());
        entity.setStatus(order.getStatus().name());
        entity.setPaymentAttempts(order.getPaymentAttempts());
        entity.setCreatedAt(order.getCreatedAt());
        // Guardamos un resumen simple de los items
        entity.setItemsJson(order.getItems().toString());
        orderRepository.save(entity);
    }
}