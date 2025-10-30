package com.softwarearchetypes.product;

import java.util.UUID;

/**
 * Simple UUID-based ProductIdentifier implementation.
 * Use when you don't need to interoperate with external standard systems (ISBN, GTIN, etc.)
 */
record UuidProductIdentifier(UUID value) implements ProductIdentifier {

    static UuidProductIdentifier newOne() {
        return new UuidProductIdentifier(UUID.randomUUID());
    }

    static UuidProductIdentifier of(String value) {
        return new UuidProductIdentifier(UUID.fromString(value));
    }

    @Override
    public String type() {
        return "UUID";
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
