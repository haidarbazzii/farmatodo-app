package com.farmatodo.challenge.application.service;

import com.farmatodo.challenge.domain.model.Cart;
import com.farmatodo.challenge.domain.model.Customer;
import com.farmatodo.challenge.domain.port.out.CartRepositoryPort;
import com.farmatodo.challenge.domain.port.out.InventoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepositoryPort cartRepository;
    private final InventoryPort inventoryPort;

    @Transactional
    public Cart addItemToCart(String email, Long productId, int quantity) {
        // 1. Validacion de existencia del producto
        if (inventoryPort.findById(productId).isEmpty()) {
            throw new IllegalArgumentException("Producto no existe ID: " + productId);
        }

        // 2. Buscar carrito; sino, crea uno nuevo
        Cart cart = cartRepository.findByCustomerEmail(email)
                .orElseGet(() -> Cart.builder()
                        .customer(Customer.builder().email(email).build())
                        .build());

        // 3. Añadir item al carrito
        cart.addItem(productId, quantity);

        // 4. Guardar
        return cartRepository.save(cart);
    }
}