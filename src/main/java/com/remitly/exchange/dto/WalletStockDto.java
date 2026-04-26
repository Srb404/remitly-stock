package com.remitly.exchange.dto;

import com.remitly.exchange.domain.WalletStock;

public record WalletStockDto(String walletId, String stockName, long quantity) {

    public static WalletStockDto from(WalletStock entity) {
        return new WalletStockDto(entity.getWalletId(), entity.getStockName(), entity.getQuantity());
    }
}