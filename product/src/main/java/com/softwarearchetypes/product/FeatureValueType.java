package com.softwarearchetypes.product;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Defines the safe set of data types that can be used for product feature values.
 * Each type knows how to convert between its runtime representation and String (for persistence).
 *
 * This enum restricts feature values to a well-defined set of types, preventing
 * arbitrary classes from being used as feature values.
 */
enum FeatureValueType {

    TEXT(String.class) {
        @Override
        Object castFrom(String value) {
            return value;
        }

        @Override
        String castTo(Object value) {
            return (String) value;
        }
    },

    INTEGER(Integer.class) {
        @Override
        Object castFrom(String value) {
            return Integer.valueOf(value);
        }

        @Override
        String castTo(Object value) {
            return String.valueOf(value);
        }
    },

    DECIMAL(BigDecimal.class) {
        @Override
        Object castFrom(String value) {
            return new BigDecimal(value);
        }

        @Override
        String castTo(Object value) {
            return value.toString();
        }
    },

    DATE(LocalDate.class) {
        @Override
        Object castFrom(String value) {
            return LocalDate.parse(value);
        }

        @Override
        String castTo(Object value) {
            return value.toString();
        }
    },

    BOOLEAN(Boolean.class) {
        @Override
        Object castFrom(String value) {
            return Boolean.valueOf(value);
        }

        @Override
        String castTo(Object value) {
            return String.valueOf(value);
        }
    };

    private final Class<?> type;

    FeatureValueType(Class<?> type) {
        this.type = type;
    }

    /**
     * Converts a String representation to the runtime type.
     * @throws IllegalArgumentException if the value cannot be parsed
     */
    abstract Object castFrom(String value);

    /**
     * Converts the runtime type to its String representation (for persistence).
     */
    abstract String castTo(Object value);

    /**
     * Returns the class that represents this value type.
     */
    Class<?> type() {
        return type;
    }

    /**
     * Checks if the given value is an instance of this type.
     */
    boolean isInstance(Object value) {
        return type.isInstance(value);
    }
}
