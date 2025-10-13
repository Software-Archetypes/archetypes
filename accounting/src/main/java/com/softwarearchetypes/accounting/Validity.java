package com.softwarearchetypes.accounting;

import java.time.Instant;

/**
 * Represents a validity period for accounting entries.
 * validFrom is inclusive, validTo is exclusive [validFrom, validTo)
 */
public record Validity(Instant validFrom, Instant validTo) {

    public static Validity until(Instant validTo) {
        return new Validity(Instant.EPOCH, validTo);
    }

    public static Validity from(Instant validFrom) {
        return new Validity(validFrom, Instant.MAX);
    }

    public static Validity between(Instant validFrom, Instant validTo) {
        return new Validity(validFrom, validTo);
    }

    public static Validity always() {
        return new Validity(Instant.EPOCH, Instant.MAX);
    }

    public boolean isValidAt(Instant instant) {
        return !instant.isBefore(validFrom) && instant.isBefore(validTo);
    }

    public boolean hasExpired(Instant instant) {
        return !instant.isBefore(validTo);
    }
}