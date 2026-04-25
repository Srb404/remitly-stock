package com.remitly.exchange.dto;

import com.remitly.exchange.domain.WalletStock;

public record WalletStockDto(String walletId, String stockName, long quantity) {

    public static WalletStockDto from(WalletStock w) {
        return new WalletStockDto(w.getWalletId(), w.getStockName(), w.getQuantity());
    }
}