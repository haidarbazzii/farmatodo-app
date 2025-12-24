package com.farmatodo.challenge.domain.port.out;

import com.farmatodo.challenge.domain.model.TransactionLog;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface TransactionLogPort {
    void saveLog(TransactionLog log);

    List<TransactionLog> findByTransactionId(UUID transactionId);
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    default void logEvent(UUID txId, String event, String status, String msg) {
        saveLog(TransactionLog.builder()
                .transactionId(txId)
                .eventType(event)
                .status(status)
                .message(msg)
                .timestamp(java.time.LocalDateTime.now())
                .build());
    }

    List<TransactionLog> findAll();
}
