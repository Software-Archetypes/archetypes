package com.softwarearchetypes.product;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Repository for CatalogEntry persistence.
 */
interface CatalogEntryRepository {
    void save(CatalogEntry entry);
    Optional<CatalogEntry> findById(CatalogEntryId id);
    Set<CatalogEntry> findAll();
    Set<CatalogEntry> findByProductType(ProductIdentifier productTypeId);
    Set<CatalogEntry> findByCategory(String category);
    void remove(CatalogEntryId id);

    static CatalogEntryRepository inMemory() {
        return new InMemoryCatalogEntryRepository();
    }
}

/**
 * In-memory implementation of CatalogEntryRepository.
 */
class InMemoryCatalogEntryRepository implements CatalogEntryRepository {

    private final Map<CatalogEntryId, CatalogEntry> storage = new HashMap<>();

    @Override
    public void save(CatalogEntry entry) {
        storage.put(entry.id(), entry);
    }

    @Override
    public Optional<CatalogEntry> findById(CatalogEntryId id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Set<CatalogEntry> findAll() {
        return new HashSet<>(storage.values());
    }

    @Override
    public Set<CatalogEntry> findByProductType(ProductIdentifier productTypeId) {
        return storage.values().stream()
            .filter(entry -> entry.productType().id().equals(productTypeId))
            .collect(Collectors.toSet());
    }

    @Override
    public Set<CatalogEntry> findByCategory(String category) {
        return storage.values().stream()
            .filter(entry -> entry.isInCategory(category))
            .collect(Collectors.toSet());
    }

    @Override
    public void remove(CatalogEntryId id) {
        storage.remove(id);
    }
}
