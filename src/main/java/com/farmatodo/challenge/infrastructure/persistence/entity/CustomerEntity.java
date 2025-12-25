package com.farmatodo.challenge.infrastructure.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Entity
@Table(name = "customers")
@Data
public class CustomerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    @Pattern(regexp = "^(\\+58)?0?\\d{10}$", message = "El teléfono debe tener un formato válido (ej: 04241234567, 4241234567 o +5804241234567)")
    private String phoneNumber;

    private String address;
}