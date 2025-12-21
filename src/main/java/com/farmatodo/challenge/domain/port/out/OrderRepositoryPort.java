package com.farmatodo.challenge.domain.port.out;

import com.farmatodo.challenge.domain.model.Order;

public interface OrderRepositoryPort {
    Order save(Order order);
}