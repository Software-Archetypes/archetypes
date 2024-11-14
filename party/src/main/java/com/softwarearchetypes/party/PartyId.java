package com.softwarearchetypes.party;

import java.util.UUID;

import static com.softwarearchetypes.common.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public record PartyId(UUID value) {
    public PartyId {
        checkArgument(value != null, "Party Id value cannot be null");
    }

    public String asString() {
        return value.toString();
    }

    public static PartyId of(UUID value) {
        return new PartyId(value);
    }
}
