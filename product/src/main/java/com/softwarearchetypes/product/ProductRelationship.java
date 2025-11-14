package com.softwarearchetypes.product;

public record ProductRelationship(
        ProductRelationshipId id,
        ProductIdentifier from,
        ProductIdentifier to,
        ProductRelationshipType type) {

    static ProductRelationship of(
            ProductRelationshipId id,
            ProductIdentifier from,
            ProductIdentifier to,
            ProductRelationshipType type) {
        return new ProductRelationship(id, from, to, type);
    }
}
