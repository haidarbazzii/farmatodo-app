package com.farmatodo.challenge.application.service;

import com.farmatodo.challenge.domain.model.*;
import com.farmatodo.challenge.domain.port.out.*;
import com.farmatodo.challenge.infrastructure.persistence.entity.CardTokenEntity;
import com.farmatodo.challenge.infrastructure.persistence.entity.CustomerEntity;
import com.farmatodo.challenge.domain.port.out.CartRepositoryPort;
import com.farmatodo.challenge.infrastructure.persistence.repository.CardTokenJpaRepository;
import com.farmatodo.challenge.infrastructure.persistence.repository.CustomerJpaRepository;
import com.farmatodo.challenge.infrastructure.persistence.repository.OrderJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderJpaRepository orderRepository;
    @Mock private InventoryPort inventoryPort;
    @Mock private NotificationPort notificationPort;
    @Mock private TransactionLogPort transactionLogPort;
    @Mock private CustomerJpaRepository customerRepository;
    @Mock private CartRepositoryPort cartRepository; //
    @Mock private CardTokenJpaRepository cardTokenRepository;


    @InjectMocks
    private OrderService orderService;

    private Order orderInput;
    private CustomerEntity customerEntity;
    private Product productStub;
    private UUID fixedToken;

    @BeforeEach
    void setUp() {
        // Configuración de propiedades privadas
        ReflectionTestUtils.setField(orderService, "maxRetries", 3);
        ReflectionTestUtils.setField(orderService, "paymentRejectionProbability", 0.0);

        fixedToken = UUID.randomUUID();

        // Datos del Cliente
        customerEntity = new CustomerEntity();
        customerEntity.setId(1L);
        customerEntity.setEmail("test@farmatodo.com");
        customerEntity.setName("Test User");
        customerEntity.setPhoneNumber("+584141234567");
        customerEntity.setAddress("Caracas");

        // Datos del Producto
        productStub = new Product(1L, "Acetaminofen", "Desc", new BigDecimal("5.00"), 100);

        // Orden de entrada básica
        orderInput = Order.builder()
                .customer(Customer.builder().email("test@farmatodo.com").build())
                .cardTokenId(fixedToken)
                .items(List.of(new OrderItem(1L, "Unknown", BigDecimal.ZERO, 2)))
                .build();
    }

    // --- TESTS DE PROCESS ORDER ---

    @Test
    @DisplayName("1. Éxito: Debe procesar orden completa (Enriquecer, Stock, Pago, Guardar)")
    void shouldProcessOrderSuccessfully() {
        when(customerRepository.findByEmail(anyString())).thenReturn(Optional.of(customerEntity));
        when(inventoryPort.findById(1L)).thenReturn(Optional.of(productStub));
        when(inventoryPort.decreaseStock(1L, 2)).thenReturn(true);

        Order result = orderService.processOrder(orderInput);

        assertEquals(OrderStatus.APPROVED, result.getStatus());
        assertEquals(new BigDecimal("10.00"), result.getTotalAmount());
        assertEquals("Test User", result.getCustomer().getName()); // Verificamos enriquecimiento

        verify(orderRepository).save(any());
        verify(notificationPort).sendEmail(contains("test@farmatodo.com"), contains("Compra Exitosa"), anyString());
    }

    @Test
    @DisplayName("2. Fallo: Debe lanzar error si el Cliente no existe")
    void shouldFailIfCustomerNotFound() {
        when(customerRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            orderService.processOrder(orderInput);
        });

        assertTrue(exception.getMessage().contains("Cliente no encontrado"));
        verifyNoInteractions(inventoryPort); // No debió ni intentar buscar productos
    }

    @Test
    @DisplayName("3. Fallo: Debe lanzar error si el Producto no existe")
    void shouldFailIfProductNotFound() {
        when(customerRepository.findByEmail(anyString())).thenReturn(Optional.of(customerEntity));
        when(inventoryPort.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            orderService.processOrder(orderInput);
        });

        assertTrue(exception.getMessage().contains("Producto no encontrado"));
    }

    @Test
    @DisplayName("4. Fallo: Debe lanzar error y Log si no hay Stock")
    void shouldFailIfStockInsufficient() {
        when(customerRepository.findByEmail(anyString())).thenReturn(Optional.of(customerEntity));
        when(inventoryPort.findById(1L)).thenReturn(Optional.of(productStub));
        when(inventoryPort.decreaseStock(1L, 2)).thenReturn(false); // <--- Stock insuficiente

        Exception exception = assertThrows(RuntimeException.class, () -> {
            orderService.processOrder(orderInput);
        });

        assertTrue(exception.getMessage().contains("Stock insuficiente"));
        // Verificamos que se guardó el log de fallo
        verify(transactionLogPort).logEvent(any(), eq("STOCK_RESERVATION"), eq("FAILURE"), anyString());
    }

    @Test
    @DisplayName("5. Fallo: Debe revertir y notificar si el Pago es rechazado")
    void shouldRejectOrderOnPaymentFailure() {
        // Forzamos el fallo de pago
        ReflectionTestUtils.setField(orderService, "paymentRejectionProbability", 1.0);

        when(customerRepository.findByEmail(anyString())).thenReturn(Optional.of(customerEntity));
        when(inventoryPort.findById(1L)).thenReturn(Optional.of(productStub));
        when(inventoryPort.decreaseStock(1L, 2)).thenReturn(true);

        assertThrows(RuntimeException.class, () -> orderService.processOrder(orderInput));

        verify(notificationPort).sendEmail(contains("test@farmatodo.com"), contains("Pago Fallido"), anyString());
        verify(transactionLogPort).logEvent(any(), eq("ORDER_FINALIZATION"), eq("REJECTED"), anyString());
    }

    // --- TESTS DE CHECKOUT CART ---

    @Test
    @DisplayName("6. Carrito: Debe procesar el carrito y luego eliminarlo")
    void shouldCheckoutCartSuccessfully() {
        // Preparar Carrito Simulado
        Cart cart = Cart.builder()
                .id(99L)
                .customer(Customer.builder().email("test@farmatodo.com").build())
                .items(List.of(new CartItem(1L, 2)))
                .build();

        // --- NUEVO: Preparar el Token Simulado ---
        CardTokenEntity cardTokenStub = new CardTokenEntity();
        // Si tu lógica valida fechas dentro de CardToken, configura una fecha válida aquí:
        // cardTokenStub.setExpDate("12/30");
        cardTokenStub.setExpirationDate(LocalDateTime.now().plusDays(1));

        // Mocks necesarios para el checkout Y para el processOrder interno
        when(cartRepository.findByCustomerEmail("test@farmatodo.com")).thenReturn(Optional.of(cart));
        when(customerRepository.findByEmail("test@farmatodo.com")).thenReturn(Optional.of(customerEntity));
        when(inventoryPort.findById(1L)).thenReturn(Optional.of(productStub));
        when(inventoryPort.decreaseStock(1L, 2)).thenReturn(true);

        when(cardTokenRepository.findById(fixedToken)).thenReturn(Optional.of(cardTokenStub));
        // Ejecutar
        Order result = orderService.checkoutCart("test@farmatodo.com", fixedToken);

        // Verificar
        assertEquals(OrderStatus.APPROVED, result.getStatus());
        // ¡LA LÍNEA MÁS IMPORTANTE DEL TEST!: Verificar que se borró el carrito
        verify(cartRepository).deleteByCustomerEmail("test@farmatodo.com");
    }

    @Test
    @DisplayName("7. Carrito: Debe fallar si el carrito no existe")
    void shouldFailIfCartNotFound() {
        CardTokenEntity tokenValido = new CardTokenEntity();
        tokenValido.setExpirationDate(LocalDateTime.now().plusDays(1)); // Fecha futura

        // Le decimos al mock: "Cuando busquen cualquier token, devuelve este válido"
        when(cardTokenRepository.findById(any())).thenReturn(Optional.of(tokenValido));

        when(cartRepository.findByCustomerEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                orderService.checkoutCart("nobody@mail.com", fixedToken)
        );
    }

    @Test
    @DisplayName("8. Carrito: Debe fallar si el carrito está vacío")
    void shouldFailIfCartIsEmpty() {
        CardTokenEntity tokenValido = new CardTokenEntity();
        tokenValido.setExpirationDate(LocalDateTime.now().plusDays(1)); // Fecha futura
        when(cardTokenRepository.findById(any())).thenReturn(Optional.of(tokenValido));

        Cart emptyCart = Cart.builder().items(new ArrayList<>()).build();
        when(cartRepository.findByCustomerEmail(anyString())).thenReturn(Optional.of(emptyCart));

        assertThrows(IllegalArgumentException.class, () ->
                orderService.checkoutCart("test@farmatodo.com", fixedToken)
        );
    }
}