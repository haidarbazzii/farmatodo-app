package com.farmatodo.challenge.infrastructure.controller;

import com.farmatodo.challenge.domain.model.Customer;
import com.farmatodo.challenge.domain.model.Order;
import com.farmatodo.challenge.domain.model.OrderItem;
import com.farmatodo.challenge.domain.port.in.OrderUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderUseCase orderUseCase;

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequest request) {
        // Mapeo DTO -> Dominio
        // Nota: En un caso real buscaríamos al cliente por ID,
        // aquí lo construimos básico para el flujo.
        Customer customer = Customer.builder()
                .email(request.customerEmail())
                .build();

        List<OrderItem> items = request.items().stream()
                .map(i -> new OrderItem(i.productId(), "Unknown", BigDecimal.ZERO, i.quantity()))
                .collect(Collectors.toList());

        Order order = Order.builder()
                .customer(customer)
                .items(items)
                .cardTokenId(request.cardTokenId())
                .build();

        return ResponseEntity.ok(orderUseCase.processOrder(order));
    }

    @PostMapping("/checkout")
    public ResponseEntity<Order> checkoutCart(@RequestBody CheckoutRequest request) {
        return ResponseEntity.ok(orderUseCase.checkoutCart(request.email(), request.cardTokenId()));
    }

    public record CheckoutRequest(String email, UUID cardTokenId) {}
    // DTOs
    public record OrderRequest(String customerEmail, UUID cardTokenId, List<OrderItemRequest> items) {}
    public record OrderItemRequest(Long productId, int quantity) {}
}
