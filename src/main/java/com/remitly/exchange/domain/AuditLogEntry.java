package com.remitly.exchange.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "audit_log")
public class AuditLogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "type", length = 8, nullable = false)
    private OperationType type;

    @Column(name = "wallet_id", length = 128, nullable = false)
    private String walletId;

    @Column(name = "stock_name", length = 128, nullable = false)
    private String stockName;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected AuditLogEntry() {
    }

    public AuditLogEntry(OperationType type, String walletId, String stockName, OffsetDateTime createdAt) {
        this.type = type;
        this.walletId = walletId;
        this.stockName = stockName;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public OperationType getType() { return type; }
    public String getWalletId() { return walletId; }
    public String getStockName() { return stockName; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}