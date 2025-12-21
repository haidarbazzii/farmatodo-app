package com.farmatodo.challenge.infrastructure.controller;

import com.farmatodo.challenge.domain.model.CardToken;
import com.farmatodo.challenge.domain.port.in.TokenizationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tokens")
@RequiredArgsConstructor
public class TokenController {

    private final TokenizationUseCase tokenizationUseCase;

    @PostMapping
    public ResponseEntity<CardToken> createToken(@RequestBody TokenRequest request) {
        CardToken token = tokenizationUseCase.createToken(
                request.pan(),
                request.cvv(),
                request.expDate()
        );
        return ResponseEntity.ok(token);
    }

    // DTO (Java Record)
    public record TokenRequest(String pan, String cvv, String expDate) {}
}