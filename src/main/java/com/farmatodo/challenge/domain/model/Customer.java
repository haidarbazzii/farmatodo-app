package com.farmatodo.challenge.domain.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
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
    @Pattern(regexp = "^(\\+58)?0?\\d{10}$", message = "El teléfono debe tener un formato válido (ej: +5804241234567)")
    private String phoneNumber;
    private String address;
}