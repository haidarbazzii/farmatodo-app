package com.farmatodo.challenge.infrastructure.persistence.repository;
import com.farmatodo.challenge.infrastructure.persistence.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerJpaRepository extends JpaRepository<CustomerEntity, Long> {
    boolean existsByEmailOrPhoneNumber(String email, String phoneNumber);

    Optional<CustomerEntity> findByEmail(String email);
}
