package com.remitly.exchange.dto;

import java.util.List;

public record BankResponse(List<BankStockDto> stocks) { }
