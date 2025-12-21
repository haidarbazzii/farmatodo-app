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
public class CardToken {
    private UUID tokenId;
    private String maskedPan;      // Solo últimos 4 dígitos
    private String encryptedData;  // PAN|CVV encriptado
    private LocalDateTime expirationDate;
    private LocalDateTime createdAt;

    public boolean isValid() {
        return LocalDateTime.now().isBefore(expirationDate);
    }
}