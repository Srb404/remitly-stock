package com.remitly.exchange.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record SetBankRequest(@NotNull @Valid List<@NotNull @Valid BankStockDto> stocks) { }
