package com.softwarearchetypes.product;

import java.util.UUID;

public record ProductRelationshipId(UUID value) {

    public static ProductRelationshipId of(UUID value) {
        return new ProductRelationshipId(value);
    }

    public static ProductRelationshipId random() {
        return new ProductRelationshipId(UUID.randomUUID());
    }

    String asString() {
        return value.toString();
    }
}
