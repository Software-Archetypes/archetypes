package com.softwarearchetypes.party;

import static com.softwarearchetypes.common.Preconditions.checkArgument;
import static com.softwarearchetypes.common.StringUtils.isNotBlank;

public record Role(String name) {

    public Role {
        checkArgument(isNotBlank(name), "Role name cannot be null");
    }

    static Role of(String value) {
        return new Role(value);
    }

    String asString() {
        return name;
    }
}
