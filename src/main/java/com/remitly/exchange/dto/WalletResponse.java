package com.remitly.exchange.dto;

import java.util.List;

public record WalletResponse(String id, List<WalletStockEntryDto> stocks) { }
