package com.farmatodo.challenge.infrastructure.adapter;

import com.farmatodo.challenge.domain.model.TransactionLog;
import com.farmatodo.challenge.domain.port.out.TransactionLogPort;
import com.farmatodo.challenge.infrastructure.persistence.entity.TransactionLogEntity;
import com.farmatodo.challenge.infrastructure.persistence.repository.TransactionLogJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TransactionLogAdapter implements TransactionLogPort {

    private final TransactionLogJpaRepository repository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveLog(TransactionLog log) {
        TransactionLogEntity entity = new TransactionLogEntity(
                log.getTransactionId(),
                log.getEventType(),
                log.getStatus(),
                log.getMessage(),
                log.getTimestamp()
        );
        repository.save(entity);
    }

    @Override
    public List<TransactionLog> findAll() {
        return repository.findAllByOrderByTimestampDesc()
                .stream()
                .map(this::toDomain)
                .limit(30).collect(Collectors.toList());
    }

    @Override
    public List<TransactionLog> findByTransactionId(UUID transactionId) {
        return repository.findByTransactionIdOrderByTimestampAsc(transactionId)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private TransactionLog toDomain(TransactionLogEntity entity) {
        return TransactionLog.builder()
                .id(entity.getId())
                .transactionId(entity.getTransactionId())
                .eventType(entity.getEventType())
                .status(entity.getStatus())
                .message(entity.getMessage())
                .timestamp(entity.getTimestamp())
                .build();
    }
}