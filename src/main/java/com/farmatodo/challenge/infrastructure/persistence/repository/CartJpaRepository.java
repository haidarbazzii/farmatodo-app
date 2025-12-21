package com.farmatodo.challenge.infrastructure.persistence.repository;

import com.farmatodo.challenge.infrastructure.persistence.entity.CartEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartJpaRepository extends JpaRepository<CartEntity, Long> {
    Optional<CartEntity> findByCustomerEmail(String email);
    void deleteByCustomerEmail(String email);
}