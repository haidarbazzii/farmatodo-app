package com.farmatodo.challenge.domain.port.out;

import com.farmatodo.challenge.domain.model.Cart;
import java.util.Optional;

public interface CartRepositoryPort {
    Optional<Cart> findByCustomerEmail(String email);
    Cart save(Cart cart);
    void deleteByCustomerEmail(String email);
}