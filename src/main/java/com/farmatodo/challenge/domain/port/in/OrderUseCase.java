package com.farmatodo.challenge.domain.port.in;

import com.farmatodo.challenge.domain.model.Order;

import java.util.UUID;

public interface OrderUseCase {
    // Procesa compra, gestiona stock y reintentos de pago
    Order processOrder(Order order);
    Order checkoutCart(String customerEmail, UUID cardTokenId);
}