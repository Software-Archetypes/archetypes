package com.softwarearchetypes.party;

import java.util.regex.Pattern;

/**
 * Phone address details with validation.
 * Supports various phone number formats including:
 * - International format with + prefix: +48123456789
 * - With spaces or dashes: +48 123 456 789, +48-123-456-789
 * - With parentheses: +48 (123) 456-789
 * - Local format: 123456789
 */
public record PhoneAddressDetails(String phoneNumber) implements AddressDetails {

    // Pattern allows optional +, digits, spaces, dashes, parentheses
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^\\+?[0-9]{1,3}?[\\s.-]?\\(?[0-9]{1,4}\\)?[\\s.-]?[0-9]{1,4}[\\s.-]?[0-9]{1,9}$"
    );

    private static final int MIN_LENGTH = 7;
    private static final int MAX_LENGTH = 20;

    public PhoneAddressDetails {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("Phone number cannot be null or empty");
        }

        String normalized = phoneNumber.replaceAll("[\\s.()-]", "");

        if (normalized.length() < MIN_LENGTH || normalized.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Phone number must be between " + MIN_LENGTH + " and " + MAX_LENGTH + " digits: " + phoneNumber
            );
        }

        if (!PHONE_PATTERN.matcher(phoneNumber).matches()) {
            throw new IllegalArgumentException("Invalid phone number format: " + phoneNumber);
        }
    }

    public static PhoneAddressDetails of(String phoneNumber) {
        return new PhoneAddressDetails(phoneNumber);
    }

    /**
     * Returns normalized phone number (digits only, with optional + prefix).
     */
    public String normalized() {
        String result = phoneNumber.replaceAll("[\\s.()-]", "");
        if (phoneNumber.startsWith("+") && !result.startsWith("+")) {
            result = "+" + result;
        }
        return result;
    }
}
