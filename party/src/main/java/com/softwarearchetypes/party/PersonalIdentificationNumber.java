package com.softwarearchetypes.party;

import java.util.regex.Pattern;

import static java.util.Optional.ofNullable;

/**
 * Personal Identification Number (PESEL in Poland).
 * Represents a unique personal identifier for individuals.
 * PESEL consists of 11 digits with encoded birth date and checksum validation.
 */
record PersonalIdentificationNumber(String value) implements RegisteredIdentifier {

    private static final Pattern PATTERN = Pattern.compile("\\d{11}");
    // PESEL has 11 digits: YYMMDDXXXXC where:
    // YY - year of birth (last 2 digits)
    // MM - month of birth (01-12, or 21-32 for years 2000-2099)
    // DD - day of birth (01-31)
    // XXXX - serial number and sex indicator (even for women, odd for men)
    // C - checksum digit
    private static final String TYPE = "PERSONAL_IDENTIFICATION_NUMBER";
    private static final int[] CHECKSUM_WEIGHTS = {1, 3, 7, 9, 1, 3, 7, 9, 1, 3};

    PersonalIdentificationNumber {
        if (ofNullable(value).filter(it -> PATTERN.matcher(it).matches()).isEmpty()) {
            throw new IllegalArgumentException("Personal identification number does not meet syntax criteria");
        }
        if (!isValidChecksum(value)) {
            throw new IllegalArgumentException("Personal identification number has invalid checksum");
        }
    }

    static PersonalIdentificationNumber of(String value) {
        return new PersonalIdentificationNumber(value);
    }

    /**
     * Validates PESEL checksum using the standard algorithm.
     * Algorithm: multiply first 10 digits by weights [1,3,7,9,1,3,7,9,1,3],
     * sum the results, take modulo 10, subtract from 10 (if result is 10, use 0),
     * compare with the 11th digit (checksum).
     */
    private static boolean isValidChecksum(String value) {
        if (value == null || value.length() != 11) {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < 10; i++) {
            int digit = Character.getNumericValue(value.charAt(i));
            sum += digit * CHECKSUM_WEIGHTS[i];
        }

        int checksum = (10 - (sum % 10)) % 10;
        int lastDigit = Character.getNumericValue(value.charAt(10));

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
        // PESEL numbers don't expire - they are assigned for life
        return Validity.ALWAYS;
    }
}
