package com.farmatodo.challenge.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Data
public class OrderEntity {
    @Id
    private UUID id; // Usamos el transactionId como PK

    private String customerEmail;
    private BigDecimal totalAmount;
    private String status;
    private Integer paymentAttempts;
    private LocalDateTime createdAt;

    // Para simplificar el reto, guardaremos los items como texto o JSON,
    // aunque en producción usaríamos una tabla relacional @OneToMany
    @Column(columnDefinition = "TEXT")
    private String itemsJson;
}