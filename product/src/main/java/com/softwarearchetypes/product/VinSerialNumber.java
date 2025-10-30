package com.softwarearchetypes.product;

import static com.softwarearchetypes.common.Preconditions.checkArgument;

/**
 * VIN (Vehicle Identification Number) - standard serial number for vehicles.
 *
 * Format: 17 characters (uppercase letters and digits, excluding I, O, Q to avoid confusion)
 * Structure:
 * - World Manufacturer Identifier (3 chars)
 * - Vehicle Descriptor Section (6 chars)
 * - Check digit (1 char)
 * - Model year (1 char)
 * - Plant code (1 char)
 * - Sequential number (6 chars)
 *
 * Examples: "5YJ3E1EA1JF000001" (Tesla), "1HGBH41JXMN109186" (Honda)
 */
record VinSerialNumber(String value) implements SerialNumber {

    VinSerialNumber {
        checkArgument(value != null && !value.isBlank(), "VIN cannot be null or blank");
        String normalized = value.toUpperCase().replaceAll("[\\s-]", "");
        checkArgument(normalized.length() == 17,
            "VIN must be exactly 17 characters");
        checkArgument(normalized.matches("[A-HJ-NPR-Z0-9]{17}"),
            "VIN must contain only uppercase letters and digits (excluding I, O, Q)");
    }

    static VinSerialNumber of(String value) {
        return new VinSerialNumber(value);
    }

    @Override
    public String type() {
        return "VIN";
    }

    @Override
    public String toString() {
        return value;
    }
}
