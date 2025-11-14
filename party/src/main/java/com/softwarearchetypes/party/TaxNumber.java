package com.softwarearchetypes.party;

import java.util.regex.Pattern;

import static java.util.Optional.ofNullable;

/**
 * Tax Identification Number (NIP in Poland, VAT Number in EU).
 * Represents a unique tax identifier for organizations and sole traders.
 * NIP consists of 10 digits with checksum validation.
 */
record TaxNumber(String value) implements RegisteredIdentifier {

    private static final Pattern PATTERN = Pattern.compile("\\d{10}");
    // In Poland NIP has 10 digits: XXXXXXXXXX where:
    // First 9 digits - identification number
    // Last digit (10th) - checksum digit
    // Can be formatted as XXX-XXX-XX-XX or XXXXXXXXXX (both are valid)
    // EU VAT numbers have country prefix, e.g., PL1234567890
    private static final String TYPE = "TAX_NUMBER";
    private static final int[] CHECKSUM_WEIGHTS = {6, 5, 7, 2, 3, 4, 5, 6, 7};

    TaxNumber {
        if (ofNullable(value).filter(it -> PATTERN.matcher(it).matches()).isEmpty()) {
            throw new IllegalArgumentException("Tax number does not meet syntax criteria");
        }
        if (!isValidChecksum(value)) {
            throw new IllegalArgumentException("Tax number has invalid checksum");
        }
    }

    static TaxNumber of(String value) {
        return new TaxNumber(value);
    }

    /**
     * Validates NIP checksum using the standard algorithm.
     * Algorithm: multiply first 9 digits by weights [6,5,7,2,3,4,5,6,7],
     * sum the results, take modulo 11,
     * compare with the 10th digit (checksum).
     * If modulo 11 equals 10, the NIP is invalid.
     */
    private static boolean isValidChecksum(String value) {
        if (value == null || value.length() != 10) {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < 9; i++) {
            int digit = Character.getNumericValue(value.charAt(i));
            sum += digit * CHECKSUM_WEIGHTS[i];
        }

        int checksum = sum % 11;

        // If checksum is 10, NIP is invalid
        if (checksum == 10) {
            return false;
        }

        int lastDigit = Character.getNumericValue(value.charAt(9));
        return checksum == lastDigit;
    }

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public String asString() {
        return value;
    }

    @Override
    public Validity validity() {
        // Tax numbers (NIP) don't expire - they are assigned permanently to organizations/sole traders
        return Validity.ALWAYS;
    }
}
