package com.softwarearchetypes.party;

import java.time.Instant;

/**
 * Represents a registered identifier with optional validity period.
 * Examples: passport (with expiration), tax number (no expiration), ID card (with expiration).
 */
public interface RegisteredIdentifier {

    String type();

    String asString();

    /**
     * Returns the validity period for this identifier.
     * Some identifiers never expire (e.g., tax numbers) and will return Validity.ALWAYS.
     * Others have expiration dates (e.g., passports, ID cards).
     */
    Validity validity();

    /**
     * Checks if this identifier is currently valid.
     */
    default boolean isCurrentlyValid() {
        return validity().isCurrentlyValid();
    }

    /**
     * Checks if this identifier is valid at the given instant.
     */
    default boolean isValidAt(Instant instant) {
        return validity().isValidAt(instant);
    }
}
