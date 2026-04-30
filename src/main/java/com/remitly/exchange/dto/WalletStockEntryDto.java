package com.remitly.exchange.dto;

import com.remitly.exchange.domain.WalletStock;

public record WalletStockEntryDto(String name, long quantity) {

    public static WalletStockEntryDto from(WalletStock entity) {
        return new WalletStockEntryDto(entity.getStockName(), entity.getQuantity());
    }
}
