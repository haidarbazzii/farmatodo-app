package com.farmatodo.challenge.infrastructure.persistence.repository;
import com.farmatodo.challenge.infrastructure.persistence.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;
public interface TransactionLogJpaRepository extends JpaRepository<TransactionLogEntity, Long> {
    List<TransactionLogEntity> findByTransactionIdOrderByTimestampAsc(UUID transactionId);

    List<TransactionLogEntity> findAllByOrderByTimestampDesc();
}