package com.softwarearchetypes.accounting;

import java.util.UUID;

public record GroupId(UUID uuid) {

    public static GroupId generate() {
        return new GroupId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return uuid.toString();
    }
}
