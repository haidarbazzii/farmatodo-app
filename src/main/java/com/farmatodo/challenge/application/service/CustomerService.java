package com.farmatodo.challenge.application.service;

import com.farmatodo.challenge.domain.model.Customer;
import com.farmatodo.challenge.domain.port.in.CustomerUseCase;
import com.farmatodo.challenge.infrastructure.persistence.entity.CustomerEntity;
import com.farmatodo.challenge.infrastructure.persistence.repository.CustomerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service; // <--- ¡Esta anotación es la clave!

@Service
@RequiredArgsConstructor
public class CustomerService implements CustomerUseCase {

    private final CustomerJpaRepository repository;

    @Override
    public Customer registerCustomer(Customer customer) {
        // 1. Validar existencia del usuario
        if (repository.existsByEmailOrPhoneNumber(customer.getEmail(), customer.getPhoneNumber())) {
            throw new IllegalArgumentException("Customer already exists with provided email or phone.");
        }

        // 2. Mapeo Dominio -> Entidad JPA
        CustomerEntity entity = new CustomerEntity();
        entity.setName(customer.getName());
        entity.setEmail(customer.getEmail());
        entity.setPhoneNumber(customer.getPhoneNumber());
        entity.setAddress(customer.getAddress());

        // 3. Guardar en DB
        CustomerEntity saved = repository.save(entity);

        // 4. Actualizar ID en el objeto de dominio y retornarlo
        customer.setId(saved.getId());
        return customer;
    }
}
