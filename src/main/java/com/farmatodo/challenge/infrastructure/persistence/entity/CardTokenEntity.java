package com.farmatodo.challenge.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "card_tokens")
@Data
public class CardTokenEntity {
    @Id
    private UUID tokenId;
    private String maskedPan;
    private String encryptedData;
    private LocalDateTime expirationDate;
    private LocalDateTime createdAt;
}