package com.farmatodo.challenge.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
@Data
public class CartEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String customerEmail;

    // Relación con Items (Cascade ALL para que guarde/borre items automáticamente)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "cart_id")
    @ToString.Exclude
    private List<CartItemEntity> items = new ArrayList<>();
}