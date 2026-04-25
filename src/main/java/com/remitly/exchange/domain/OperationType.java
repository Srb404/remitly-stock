package com.remitly.exchange.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Locale;

public enum OperationType {
    BUY,
    SELL;

    @JsonValue
    public String jsonValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    @JsonCreator
    public static OperationType fromJson(String value) {
        if (value == null) {
            return null;
        }
        return OperationType.valueOf(value.toUpperCase(Locale.ROOT));
    }
}