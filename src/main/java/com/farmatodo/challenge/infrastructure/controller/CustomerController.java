package com.farmatodo.challenge.infrastructure.controller;

import com.farmatodo.challenge.domain.model.Customer;
import com.farmatodo.challenge.domain.port.in.CustomerUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerUseCase customerUseCase;

    @PostMapping
    public ResponseEntity<Customer> register(@RequestBody @Valid CustomerRequest request) {
        // Mapeo DTO -> Dominio
        Customer customer = Customer.builder()
                .name(request.name())
                .email(request.email())
                .phoneNumber(request.phoneNumber())
                .address(request.address())
                .build();

        return ResponseEntity.ok(customerUseCase.registerCustomer(customer));
    }

    public record CustomerRequest(
            @NotBlank String name,
            @Email(message = "El formato del correo electrónico es inválido") String email,
            @NotBlank String phoneNumber,
            String address
    ) {}
}