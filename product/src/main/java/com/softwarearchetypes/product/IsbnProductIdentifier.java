package com.softwarearchetypes.product;

import static com.softwarearchetypes.common.Preconditions.checkArgument;

/**
 * ISBN (International Standard Book Number) - standard identifier for books.
 * Format: ISBN-10 (e.g., 0-201-77060-1)
 *
 * Structure:
 * - Group identifier (geographic/language area)
 * - Publisher identifier
 * - Title identifier
 * - Check digit (0-9 or X for 10)
 */
record IsbnProductIdentifier(String value) implements ProductIdentifier {

    IsbnProductIdentifier {
        checkArgument(value != null && !value.isBlank(), "ISBN cannot be null or blank");
        // Remove hyphens and spaces for validation
        String normalized = value.replaceAll("[-\\s]", "");
        checkArgument(normalized.matches("\\d{9}[\\dX]"),
            "ISBN must be 10 digits with optional check digit X");
        checkArgument(isValidCheckDigit(normalized), "Invalid ISBN check digit");
    }

    static IsbnProductIdentifier of(String value) {
        return new IsbnProductIdentifier(value);
    }

    @Override
    public String type() {
        return "ISBN";
    }

    @Override
    public String toString() {
        return "ISBN " + value;
    }

    private static boolean isValidCheckDigit(String isbn) {
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += (10 - i) * Character.getNumericValue(isbn.charAt(i));
        }
        char checkChar = isbn.charAt(9);
        int checkDigit = (checkChar == 'X') ? 10 : Character.getNumericValue(checkChar);
        return (sum + checkDigit) % 11 == 0;
    }
}
