package com.farmatodo.challenge.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionLog {
    private Long id;
    private UUID transactionId;      // El hilo conductor (Trace ID)
    private String eventType;        // Ej: "PAYMENT_ATTEMPT", "TOKENIZATION", "STOCK_RESERVATION"
    private String status;           // "SUCCESS", "FAILURE"
    private String message;          // Detalle: "Rechazado por probabilidad"
    private LocalDateTime timestamp;

    // Opcional: Podríamos guardar un JSON con payload si fuera necesario
}