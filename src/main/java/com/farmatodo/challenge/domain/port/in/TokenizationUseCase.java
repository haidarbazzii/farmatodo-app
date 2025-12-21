package com.farmatodo.challenge.domain.port.in;

import com.farmatodo.challenge.domain.model.CardToken;

public interface TokenizationUseCase {
    // Recibe datos crudos, devuelve token seguro
    CardToken createToken(String pan, String cvv, String expDate);
}