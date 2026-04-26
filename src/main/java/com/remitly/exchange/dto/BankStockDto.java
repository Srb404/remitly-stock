package com.remitly.exchange.dto;

import com.remitly.exchange.domain.BankStock;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record BankStockDto(
        @NotBlank String name,
        @Min(0) long quantity) {

    public static BankStockDto from(BankStock entity) {
        return new BankStockDto(entity.getName(), entity.getQuantity());
    }
}