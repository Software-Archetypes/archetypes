package com.softwarearchetypes.accounting;

import java.util.UUID;

public record AccountId(UUID uuid) {

    public static AccountId generate() {
        return new AccountId(UUID.randomUUID());
    }

    public static AccountId of(UUID uuid) {
        return new AccountId(uuid);
    }

    @Override
    public String toString() {
        return uuid.toString();
    }
}
