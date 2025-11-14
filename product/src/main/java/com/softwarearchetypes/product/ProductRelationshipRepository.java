package com.softwarearchetypes.product;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public interface ProductRelationshipRepository {

    List<ProductRelationship> findAllRelationsFrom(ProductIdentifier productIdentifier);

    List<ProductRelationship> findAllRelationsFrom(ProductIdentifier productIdentifier, ProductRelationshipType type);

    Optional<ProductRelationship> findBy(ProductRelationshipId relationshipId);

    void save(ProductRelationship productRelationship);

    Optional<ProductRelationshipId> delete(ProductRelationshipId relationshipId);

    List<ProductRelationship> findMatching(Predicate<ProductRelationship> predicate);
}

class InMemoryProductRelationshipRepository implements ProductRelationshipRepository {

    private final ConcurrentHashMap<ProductRelationshipId, ProductRelationship> map = new ConcurrentHashMap<>();

    @Override
    public List<ProductRelationship> findAllRelationsFrom(ProductIdentifier productIdentifier) {
        return map.values().stream()
                .filter(rel -> rel.from().equals(productIdentifier))
                .toList();
    }

    @Override
    public List<ProductRelationship> findAllRelationsFrom(ProductIdentifier productIdentifier, ProductRelationshipType type) {
        return map.values().stream()
                .filter(rel -> rel.from().equals(productIdentifier) && rel.type().equals(type))
                .toList();
    }

    @Override
    public Optional<ProductRelationship> findBy(ProductRelationshipId relationshipId) {
        return Optional.ofNullable(map.get(relationshipId));
    }

    @Override
    public void save(ProductRelationship productRelationship) {
        map.put(productRelationship.id(), productRelationship);
    }

    @Override
    public Optional<ProductRelationshipId> delete(ProductRelationshipId relationshipId) {
        ProductRelationship removed = map.remove(relationshipId);
        return Optional.ofNullable(removed).map(ProductRelationship::id);
    }

    @Override
    public List<ProductRelationship> findMatching(Predicate<ProductRelationship> predicate) {
        return map.values().stream()
                .filter(predicate)
                .toList();
    }
}
