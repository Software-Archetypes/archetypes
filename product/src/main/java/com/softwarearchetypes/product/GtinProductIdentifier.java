package com.softwarearchetypes.product;

import static com.softwarearchetypes.common.Preconditions.checkArgument;

/**
 * GTIN (Global Trade Item Number) - standard identifier for retail products.
 * Supports multiple data structures:
 * - GTIN-8 (EAN-8): 8 digits
 * - GTIN-12 (UPC-A): 12 digits
 * - GTIN-13 (EAN-13): 13 digits
 * - GTIN-14: 14 digits
 *
 * All include company prefix, item reference number, and check digit.
 */
record GtinProductIdentifier(String value) implements ProductIdentifier {

    GtinProductIdentifier {
        checkArgument(value != null && !value.isBlank(), "GTIN cannot be null or blank");
        String normalized = value.replaceAll("[-\\s]", "");
        checkArgument(normalized.matches("\\d{8}|\\d{12}|\\d{13}|\\d{14}"),
            "GTIN must be 8, 12, 13, or 14 digits");
        checkArgument(isValidCheckDigit(normalized), "Invalid GTIN check digit");
    }

    static GtinProductIdentifier of(String value) {
        return new GtinProductIdentifier(value);
    }

    @Override
    public String type() {
        return "GTIN-" + value.length();
    }

    @Override
    public String toString() {
        return value;
    }

    private static boolean isValidCheckDigit(String gtin) {
        int sum = 0;
        for (int i = 0; i < gtin.length() - 1; i++) {
            int digit = Character.getNumericValue(gtin.charAt(i));
            // Alternate between multiplying by 3 and 1, starting from the right
            int multiplier = ((gtin.length() - i) % 2 == 0) ? 3 : 1;
            sum += digit * multiplier;
        }
        int checkDigit = Character.getNumericValue(gtin.charAt(gtin.length() - 1));
        int calculatedCheck = (10 - (sum % 10)) % 10;
        return checkDigit == calculatedCheck;
    }
}
