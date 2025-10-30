package com.softwarearchetypes.product;

import static com.softwarearchetypes.common.Preconditions.checkArgument;

/**
 * Descriptive name for a Batch (e.g., "Batch 2024-Q1-001", "LOT-20240115-A").
 */
record BatchName(String value) {

    BatchName {
        checkArgument(value != null && !value.isBlank(), "BatchName cannot be null or blank");
    }

    static BatchName of(String value) {
        return new BatchName(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
