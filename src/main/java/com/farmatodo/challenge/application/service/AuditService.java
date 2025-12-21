package com.farmatodo.challenge.application.service;

import com.farmatodo.challenge.domain.model.TransactionLog;
import com.farmatodo.challenge.domain.port.out.TransactionLogPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final TransactionLogPort transactionLogPort;

    public List<TransactionLog> getLogs(UUID transactionId) {
        return transactionLogPort.findByTransactionId(transactionId);
    }

    public List<TransactionLog> getAllLogs() {
        return transactionLogPort.findAll();
    }
}