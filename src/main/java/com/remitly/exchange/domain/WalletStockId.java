package com.remitly.exchange.domain;

import java.io.Serializable;
import java.util.Objects;

public class WalletStockId implements Serializable {

    private String walletId;
    private String stockName;

    public WalletStockId() {
    }

    public WalletStockId(String walletId, String stockName) {
        this.walletId = walletId;
        this.stockName = stockName;
    }

    public String getWalletId() {
        return walletId;
    }

    public String getStockName() {
        return stockName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WalletStockId other)) return false;
        return Objects.equals(walletId, other.walletId) && Objects.equals(stockName, other.stockName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(walletId, stockName);
    }
}