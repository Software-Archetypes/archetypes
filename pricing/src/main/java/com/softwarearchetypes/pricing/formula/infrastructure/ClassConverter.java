package com.softwarearchetypes.pricing.formula.infrastructure;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
class ClassConverter implements AttributeConverter<Class<?>, String> {

    @Override
    public String convertToDatabaseColumn(Class attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getName();
    }

    @Override
    public Class<?> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            return Class.forName(dbData);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Invalid class name: " + dbData, e);
        }
    }
}