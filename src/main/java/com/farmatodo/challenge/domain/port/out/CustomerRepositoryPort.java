package com.farmatodo.challenge.domain.port.out;

import com.farmatodo.challenge.domain.model.Customer;

public interface CustomerRepositoryPort {
    Customer save(Customer customer);
    boolean existsByEmailOrPhone(String email, String phone);
}
