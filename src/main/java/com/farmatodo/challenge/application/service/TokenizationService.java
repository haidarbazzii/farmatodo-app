package com.farmatodo.challenge.application.service;

import com.farmatodo.challenge.domain.model.CardToken;
import com.farmatodo.challenge.domain.port.in.TokenizationUseCase;
import com.farmatodo.challenge.infrastructure.persistence.repository.CardTokenJpaRepository;
import com.farmatodo.challenge.infrastructure.persistence.entity.CardTokenEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.Clock;
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenizationService implements TokenizationUseCase {

    private final CardTokenJpaRepository tokenRepository;

    @Value("${app.business.tokenization.rejection-probability:0.1}")
    private double rejectionProbability;

    @Value("${app.security.encryption-key}")
    private String encryptionKey; // Debe ser 16 chars para AES-128

    public double getRejectionProbability() {
        return rejectionProbability;
    }

    public void setTokenRejectionProbability(double probability) {
        if (probability < 0 || probability > 1) {
            throw new IllegalArgumentException("La probabilidad debe estar entre 0.0 y 1.0");
        }
        this.rejectionProbability = probability;
        log.info("ADMIN: Probabilidad de fallo en tokenización actualizada a: {}", probability);
    }

    @Override
    public CardToken createToken(String pan, String cvv, String expDate) {
        // 1. Probabilidad de rechazo
        if (ThreadLocalRandom.current().nextDouble() < rejectionProbability) {

            throw new RuntimeException("Tokenization rejected due to high risk score.");
        }

        if (pan.length() != 16){
            throw new IllegalArgumentException("Numero de Tarjeta invalido");
        }

        if (cvv.length() != 3){
            throw new IllegalArgumentException("CVV invalido");
        }

        boolean cardDateValid = isCardValid(expDate) ;

        if (!cardDateValid){
            throw new IllegalArgumentException("La Trajeta esta expirada");
        }

        // 2. Encriptación (AES)
        String rawData = pan + "|" + cvv + "|" + expDate;
        String encryptedData = encrypt(rawData);

        // 3. Enmascaramiento (Masking)
        String maskedPan = "******" + pan.substring(Math.max(0, pan.length() - 4));

        // 4. Persistencia
        CardTokenEntity entity = new CardTokenEntity();
        entity.setTokenId(UUID.randomUUID());
        entity.setMaskedPan(maskedPan);
        entity.setEncryptedData(encryptedData);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setExpirationDate(LocalDateTime.now().plusMinutes(20));

        tokenRepository.save(entity);

        // 5. Retorno al dominio
        return CardToken.builder()
                .tokenId(entity.getTokenId())
                .maskedPan(maskedPan)
                .encryptedData(encryptedData)
                .expirationDate(entity.getExpirationDate())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private String encrypt(String data) {
        try {
            var key = new SecretKeySpec(encryptionKey.getBytes(), "AES");
            var cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting sensitive data", e);
        }
    }

    private boolean isCardValid (String expDate){
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yy");
            try {
                YearMonth expiryDate = YearMonth.parse(expDate, formatter);
                YearMonth currentMonth = YearMonth.now(Clock.systemDefaultZone());
                return !expiryDate.isBefore(currentMonth);

            } catch (DateTimeParseException e) {
                return false;
            }
    }
}
