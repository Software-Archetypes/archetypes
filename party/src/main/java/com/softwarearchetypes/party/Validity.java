package com.softwarearchetypes.party;

import java.time.Instant;

/**
 * Represents a validity period for registered identifiers (e.g., passport, ID card expiration).
 * validFrom is inclusive, validTo is exclusive [validFrom, validTo)
 */
public record Validity(Instant validFrom, Instant validTo) {

    public static final Validity ALWAYS = new Validity(Instant.EPOCH, Instant.MAX);

    public static Validity until(Instant validTo) {
        return new Validity(Instant.EPOCH, validTo);
    }

    public static Validity from(Instant validFrom) {
        return new Validity(validFrom, Instant.MAX);
    }

    public static Validity between(Instant validFrom, Instant validTo) {
        if (validFrom == null && validTo == null) {
            return ALWAYS;
        }
        if (validFrom == null) {
            return until(validTo);
        }
        if (validTo == null) {
            return from(validFrom);
        }
        return new Validity(validFrom, validTo);
    }

    public static Validity always() {
        return ALWAYS;
    }

    public boolean isValidAt(Instant instant) {
        return !instant.isBefore(validFrom) && instant.isBefore(validTo);
    }

    public boolean hasExpired(Instant instant) {
        return !instant.isBefore(validTo);
    }

    public boolean isCurrentlyValid() {
        return isValidAt(Instant.now());
    }

    public boolean overlaps(Validity other) {
        // Two periods overlap if one starts before the other ends
        // [a, b) overlaps [c, d) if a < d AND c < b
        return this.validFrom.isBefore(other.validTo) && other.validFrom.isBefore(this.validTo);
    }
}
