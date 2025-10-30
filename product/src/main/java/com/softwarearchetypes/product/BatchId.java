package com.softwarearchetypes.product;

import java.util.UUID;

/**
 * Unique identifier for a Batch.
 */
record BatchId(UUID value) {

    static BatchId newOne() {
        return new BatchId(UUID.randomUUID());
    }

    static BatchId of(String value) {
        return new BatchId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
