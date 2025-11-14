package com.softwarearchetypes.party;

import java.util.regex.Pattern;

import static java.util.Optional.ofNullable;

/**
 * Passport as a RegisteredIdentifier.
 * Unlike PESEL or NIP, passports have expiration dates and must be renewed.
 */
public record Passport(String number, Validity validity) implements RegisteredIdentifier {

    private static final Pattern PATTERN = Pattern.compile("[A-Z]{2}\\d{7}");
    // Polish passport format: 2 letters followed by 7 digits, e.g., AB1234567
    // Different countries have different formats
    private static final String TYPE = "PASSPORT";

    public Passport {
        if (ofNullable(number).filter(it -> PATTERN.matcher(it).matches()).isEmpty()) {
            throw new IllegalArgumentException("Passport number does not meet syntax criteria");
        }
        if (validity == null) {
            throw new IllegalArgumentException("Passport must have validity period");
        }
    }

    public static Passport of(String number, Validity validity) {
        return new Passport(number, validity);
    }

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public String asString() {
        return number;
    }
}
