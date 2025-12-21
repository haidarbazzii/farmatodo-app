package com.farmatodo.challenge.infrastructure.controller;

import com.farmatodo.challenge.application.service.AuditService;
import com.farmatodo.challenge.domain.model.TransactionLog;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/{transactionId}")
    public ResponseEntity<List<TransactionLog>> getTransactionLogs(@PathVariable UUID transactionId) {
        List<TransactionLog> logs = auditService.getLogs(transactionId);

        if (logs.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(logs);
    }

    @GetMapping
    public ResponseEntity<List<TransactionLog>> getAllLogs() {
        return ResponseEntity.ok(auditService.getAllLogs());
    }
}