package com.remitly.exchange.dto;

import com.remitly.exchange.domain.AuditLogEntry;
import com.remitly.exchange.domain.OperationType;
import java.time.OffsetDateTime;

public record AuditLogEntryDto(
        Long id,
        OperationType type,
        String walletId,
        String stockName,
        OffsetDateTime createdAt) {

    public static AuditLogEntryDto from(AuditLogEntry entity) {
        return new AuditLogEntryDto(
                entity.getId(),
                entity.getType(),
                entity.getWalletId(),
                entity.getStockName(),
                entity.getCreatedAt());
    }
}