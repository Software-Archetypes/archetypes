package com.softwarearchetypes.product;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a time period during which something is valid (available, active, etc.).
 * Both boundaries are optional:
 * - No from date = valid since the beginning of time
 * - No to date = valid indefinitely
 */
class Validity {

    private final LocalDate from;
    private final LocalDate to;

    private Validity(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("From date must be before or equal to date");
        }
        this.from = from;
        this.to = to;
    }

    /**
     * Creates validity period from given date (inclusive) with no end date.
     */
    static Validity from(LocalDate from) {
        return new Validity(from, null);
    }

    /**
     * Creates validity period until given date (inclusive) with no start date.
     */
    static Validity until(LocalDate to) {
        return new Validity(null, to);
    }

    /**
     * Creates validity period between two dates (both inclusive).
     */
    static Validity between(LocalDate from, LocalDate to) {
        return new Validity(from, to);
    }

    /**
     * Creates validity period with no boundaries (always valid).
     */
    static Validity always() {
        return new Validity(null, null);
    }

    /**
     * Checks if the given date falls within this validity period.
     */
    boolean isValidAt(LocalDate date) {
        if (date == null) {
            return false;
        }
        if (from != null && date.isBefore(from)) {
            return false;
        }
        if (to != null && date.isAfter(to)) {
            return false;
        }
        return true;
    }

    LocalDate from() {
        return from;
    }

    LocalDate to() {
        return to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Validity validity = (Validity) o;
        return Objects.equals(from, validity.from) && Objects.equals(to, validity.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }

    @Override
    public String toString() {
        if (from == null && to == null) {
            return "always";
        }
        if (from == null) {
            return "until " + to;
        }
        if (to == null) {
            return "from " + from;
        }
        return from + " to " + to;
    }
}
