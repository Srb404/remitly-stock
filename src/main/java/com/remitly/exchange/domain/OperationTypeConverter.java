package com.remitly.exchange.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Locale;

@Converter(autoApply = true)
public class OperationTypeConverter implements AttributeConverter<OperationType, String> {

    @Override
    public String convertToDatabaseColumn(OperationType attribute) {
        return attribute == null ? null : attribute.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public OperationType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : OperationType.valueOf(dbData.toUpperCase(Locale.ROOT));
    }
}