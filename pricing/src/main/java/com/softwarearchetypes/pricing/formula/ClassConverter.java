package com.softwarearchetypes.pricing.formula;

class ClassConverter {

    static String convertToDatabaseColumn(Class<?> attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getName();
    }

    static Class<?> convertToEntityAttribute(String dbData) {
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