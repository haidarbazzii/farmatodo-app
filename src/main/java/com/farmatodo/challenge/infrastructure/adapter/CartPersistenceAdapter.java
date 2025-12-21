package com.farmatodo.challenge.infrastructure.adapter;

import com.farmatodo.challenge.domain.model.Cart;
import com.farmatodo.challenge.domain.model.CartItem;
import com.farmatodo.challenge.domain.port.out.CartRepositoryPort;
import com.farmatodo.challenge.infrastructure.persistence.entity.CartEntity;
import com.farmatodo.challenge.infrastructure.persistence.entity.CartItemEntity;
import com.farmatodo.challenge.infrastructure.persistence.repository.CartJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CartPersistenceAdapter implements CartRepositoryPort {

    private final CartJpaRepository repository;

    @Override
    public Optional<Cart> findByCustomerEmail(String email) {
        return repository.findByCustomerEmail(email).map(this::toDomain);
    }

    @Override
    public Cart save(Cart cart) {
        CartEntity entity = repository.findByCustomerEmail(cart.getCustomer().getEmail())
                .orElse(new CartEntity());

        entity.setCustomerEmail(cart.getCustomer().getEmail());

        // Reemplazamos la lista de items (Hibernate maneja la actualización)
        entity.getItems().clear();
        entity.getItems().addAll(cart.getItems().stream()
                .map(item -> new CartItemEntity(null, item.getProductId(), item.getQuantity()))
                .collect(Collectors.toList()));

        CartEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void deleteByCustomerEmail(String email) {
        repository.deleteByCustomerEmail(email);
    }

    private Cart toDomain(CartEntity entity) {
        // Usamos el email del customer como referencia
        com.farmatodo.challenge.domain.model.Customer customer = com.farmatodo.challenge.domain.model.Customer.builder()
                .email(entity.getCustomerEmail())
                .build();

        return Cart.builder()
                .id(entity.getId())
                .customer(customer)
                .items(entity.getItems().stream()
                        .map(i -> new CartItem(i.getProductId(), i.getQuantity()))
                        .collect(Collectors.toList()))
                .build();
    }
}
