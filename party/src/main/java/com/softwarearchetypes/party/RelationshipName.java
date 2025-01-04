package com.softwarearchetypes.party;

import static com.softwarearchetypes.common.Preconditions.checkArgument;
import static com.softwarearchetypes.common.StringUtils.isNotBlank;

public record RelationshipName(String value) {

    public RelationshipName {
        checkArgument(isNotBlank(value), "Relationship name cannot be null");
    }

    static RelationshipName of(String value) {
        return new RelationshipName(value);
    }

    String asString() {
        return value;
    }

}
