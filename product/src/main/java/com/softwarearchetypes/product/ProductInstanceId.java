package com.softwarearchetypes.product;

import java.util.UUID;

/**
 * Unique identifier for a ProductInstance.
 *
 * ProductInstance needs its own identifier because:
 * - SerialNumber is optional (some instances are tracked only by Batch)
 * - Batch is optional (some instances have only SerialNumber)
 * - At least one of them must exist, but they serve different business purposes
 * - We need a consistent primary key for database persistence regardless of which tracking method is used
 */
record ProductInstanceId(UUID value) {

    static ProductInstanceId newOne() {
        return new ProductInstanceId(UUID.randomUUID());
    }

    static ProductInstanceId of(String value) {
        return new ProductInstanceId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
