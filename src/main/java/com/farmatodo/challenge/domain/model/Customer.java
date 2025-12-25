package com.farmatodo.challenge.domain.model;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Customer {
    private Long id;
    private String name;
    @Email(message = "El formato del correo electrónico es inválido")
    private String email;
    private String phoneNumber;
    private String address;
}