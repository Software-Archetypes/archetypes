package com.softwarearchetypes.product;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class ProductRelationshipsQueries {

    private final ProductRelationshipRepository repository;

    ProductRelationshipsQueries(ProductRelationshipRepository repository) {
        this.repository = repository;
    }

    public Optional<ProductRelationship> findBy(ProductRelationshipId relationshipId) {
        return repository.findBy(relationshipId);
    }

    public List<ProductRelationship> findAllRelationsFrom(ProductIdentifier productIdentifier) {
        return repository.findAllRelationsFrom(productIdentifier);
    }

    public List<ProductRelationship> findAllRelationsFrom(ProductIdentifier productIdentifier, ProductRelationshipType type) {
        return repository.findAllRelationsFrom(productIdentifier, type);
    }

    public List<ProductRelationship> findAllRelationsFrom(List<ProductIdentifier> productIdentifiers, ProductRelationshipType type) {
        return productIdentifiers.stream()
                .flatMap(it -> repository.findAllRelationsFrom(it, type).stream())
                .toList();
    }

    public List<ProductRelationship> findMatching(Predicate<ProductRelationship> predicate) {
        return repository.findMatching(predicate);
    }
}
