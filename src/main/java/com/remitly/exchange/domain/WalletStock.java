package com.remitly.exchange.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "wallet_stocks")
@IdClass(WalletStockId.class)
public class WalletStock {

    @Id
    @Column(name = "wallet_id", length = 128, nullable = false)
    private String walletId;

    @Id
    @Column(name = "stock_name", length = 128, nullable = false)
    private String stockName;

    @Column(name = "quantity", nullable = false)
    private long quantity;

    protected WalletStock() {
    }

    public WalletStock(String walletId, String stockName, long quantity) {
        this.walletId = walletId;
        this.stockName = stockName;
        this.quantity = quantity;
    }

    public String getWalletId() {
        return walletId;
    }

    public String getStockName() {
        return stockName;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }
}