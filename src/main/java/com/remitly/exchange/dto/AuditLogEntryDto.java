package com.remitly.exchange.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.remitly.exchange.domain.AuditLogEntry;
import com.remitly.exchange.domain.OperationType;

public record AuditLogEntryDto(
        OperationType type,
        @JsonProperty("wallet_id") String walletId,
        @JsonProperty("stock_name") String stockName) {

    public static AuditLogEntryDto from(AuditLogEntry entity) {
        return new AuditLogEntryDto(
                entity.getType(),
                entity.getWalletId(),
                entity.getStockName());
    }
}
