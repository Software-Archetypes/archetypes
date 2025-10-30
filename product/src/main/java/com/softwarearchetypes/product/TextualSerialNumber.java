package com.softwarearchetypes.product;

import static com.softwarearchetypes.common.Preconditions.checkArgument;

/**
 * TextualSerialNumber is a simple string-based serial number without special validation.
 *
 * Use this when:
 * - No industry-specific format is required
 * - You have your own internal numbering scheme
 * - The serial number is just a unique string
 *
 * Examples: "HYP/2024/00123", "CONS-2024-001", "PKG-2024-XYZ789"
 */
record TextualSerialNumber(String value) implements SerialNumber {

    TextualSerialNumber {
        checkArgument(value != null && !value.isBlank(),
            "SerialNumber cannot be null or blank");
    }

    static TextualSerialNumber of(String value) {
        return new TextualSerialNumber(value);
    }

    @Override
    public String type() {
        return "TEXTUAL";
    }

    @Override
    public String toString() {
        return value;
    }
}
