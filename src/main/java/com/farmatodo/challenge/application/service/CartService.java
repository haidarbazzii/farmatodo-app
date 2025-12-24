package com.farmatodo.challenge.application.service;

import com.farmatodo.challenge.domain.model.Cart;
import com.farmatodo.challenge.domain.model.Customer;
import com.farmatodo.challenge.domain.port.out.CartRepositoryPort;
import com.farmatodo.challenge.domain.port.out.InventoryPort;
import com.farmatodo.challenge.infrastructure.persistence.entity.CustomerEntity;
import com.farmatodo.challenge.infrastructure.persistence.repository.CustomerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepositoryPort cartRepository;
    private final InventoryPort inventoryPort;
    private final CustomerJpaRepository customerRepository;

    @Transactional
    public Cart addItemToCart(String email, Long productId, int quantity) {
        // 1. Validacion de existencia del producto
        if (inventoryPort.findById(productId).isEmpty()) {
            throw new IllegalArgumentException("Producto no existe ID: " + productId);
        }

        int currStock = inventoryPort.findById(productId).get().getStock();
        // 2. Buscar carrito; sino, crea uno nuevo

        CustomerEntity customerReal = customerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("El cliente con email " + email + " no existe"));
        Customer customerCart = new Customer(customerReal.getId(),customerReal.getName(),customerReal.getEmail(),customerReal.getPhoneNumber(),customerReal.getAddress());

        Cart cart = cartRepository.findByCustomerEmail(email)
                .orElseGet(() -> Cart.builder()
                        .customer(customerCart).build());

        System.out.println(cart.getCustomer().getPhoneNumber());

        // 3. Añadir item al carrito

        cart.addItem(productId, quantity, currStock);

        // 4. Guardar
        return cartRepository.save(cart);
    }
}