package com.softwarearchetypes.accounting;

import java.util.UUID;

public record EntryId(UUID value) {

    public static EntryId generate() {
        return new EntryId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
