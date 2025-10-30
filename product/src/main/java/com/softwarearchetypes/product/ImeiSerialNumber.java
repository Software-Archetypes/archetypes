package com.softwarearchetypes.product;

import static com.softwarearchetypes.common.Preconditions.checkArgument;

/**
 * IMEI (International Mobile Equipment Identity) - standard serial number for mobile devices.
 *
 * Format: 15 digits
 * Structure:
 * - TAC (Type Allocation Code): 8 digits (identifies manufacturer and model)
 * - SNR (Serial Number): 6 digits
 * - CD (Check Digit): 1 digit (Luhn algorithm)
 *
 * Examples: "123456789012345", "490154203237518"
 */
record ImeiSerialNumber(String value) implements SerialNumber {

    ImeiSerialNumber {
        checkArgument(value != null && !value.isBlank(), "IMEI cannot be null or blank");
        String normalized = value.replaceAll("[\\s-]", "");
        checkArgument(normalized.matches("\\d{15}"),
                "IMEI must be exactly 15 digits");
        checkArgument(isValidLuhnChecksum(normalized),
                "Invalid IMEI check digit (Luhn algorithm)");
    }

    static ImeiSerialNumber of(String value) {
        return new ImeiSerialNumber(value);
    }

    @Override
    public String type() {
        return "IMEI";
    }

    @Override
    public String toString() {
        return value;
    }

    private static boolean isValidLuhnChecksum(String imei) {
        int sum = 0;
        boolean alternate = false;

        for (int i = imei.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(imei.charAt(i));

            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }

            sum += digit;
            alternate = !alternate;
        }

        return sum % 10 == 0;
    }
}
