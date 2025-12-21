package com.farmatodo.challenge.domain.port.in;

import com.farmatodo.challenge.domain.model.Customer;

public interface CustomerUseCase {
    Customer registerCustomer(Customer customer);
}