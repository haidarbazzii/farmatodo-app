package com.farmatodo.challenge.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transaction_logs")
@Data
@NoArgsConstructor // Requerido por JPA
public class TransactionLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID transactionId;
    private String eventType;
    private String status;
    private String message;
    private LocalDateTime timestamp;

    public TransactionLogEntity(UUID transactionId, String eventType, String status, String message, LocalDateTime timestamp) {
        this.transactionId = transactionId;
        this.eventType = eventType;
        this.status = status;
        this.message = message;
        this.timestamp = timestamp;
    }
}