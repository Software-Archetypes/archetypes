package com.softwarearchetypes.party;

import java.util.UUID;

import static com.softwarearchetypes.common.Preconditions.checkNotNull;

public record AddressId(UUID value) {

    public AddressId {
        checkNotNull(value, "Address ID needs to be valid UUID");
    }

    public static AddressId of(UUID value) {
        return new AddressId(value);
    }

    public String asString() {
        return value.toString();
    }
}
