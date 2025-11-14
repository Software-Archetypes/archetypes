package com.softwarearchetypes.party;

import java.util.regex.Pattern;

/**
 * Email address details with validation.
 * Validates email format according to RFC 5322 simplified pattern.
 */
public record EmailAddressDetails(String email) implements AddressDetails {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    public EmailAddressDetails {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email address cannot be null or empty");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email address format: " + email);
        }
    }

    public static EmailAddressDetails of(String email) {
        return new EmailAddressDetails(email);
    }
}
