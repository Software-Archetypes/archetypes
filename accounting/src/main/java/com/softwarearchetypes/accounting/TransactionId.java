package com.softwarearchetypes.accounting;

import java.util.UUID;

public record TransactionId(UUID value) {

    public static TransactionId generate() {
        return new TransactionId(UUID.randomUUID());
    }

    public static TransactionId of(UUID uuid) {
        return new TransactionId(uuid);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
