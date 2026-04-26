package com.remitly.exchange.dto;

import com.remitly.exchange.domain.OperationType;
import jakarta.validation.constraints.NotNull;

public record TradeRequest(@NotNull OperationType type) { }