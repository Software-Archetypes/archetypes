package com.softwarearchetypes.product;

import java.time.LocalDate;
import java.util.*;

import static com.softwarearchetypes.common.Preconditions.checkArgument;

/**
 * CatalogEntry - commercial offering position.
 * Represents what the organization currently offers to customers.
 *
 * Product (ProductType or PackageType) says what something IS (business/operational definition).
 * CatalogEntry says that something is FOR SALE (commercial availability).
 *
 * Key differences from Product:
 * - displayName: marketing name (vs technical name)
 * - description: sales copy (vs technical description)
 * - categories: for navigation/search
 * - validity: when it's available for purchase
 * - metadata: flexible attributes (featured, badges, promotions, etc.)
 */
class CatalogEntry {

    private final CatalogEntryId id;
    private final String displayName;
    private final String description;
    private final Product product;
    private final Set<String> categories;
    private final Validity validity;
    private final Map<String, String> metadata;

    private CatalogEntry(CatalogEntryId id,
                        String displayName,
                        String description,
                        Product product,
                        Set<String> categories,
                        Validity validity,
                        Map<String, String> metadata) {
        checkArgument(id != null, "CatalogEntryId must be defined");
        checkArgument(displayName != null && !displayName.isBlank(), "Display name must be defined");
        checkArgument(description != null && !description.isBlank(), "Description must be defined");
        checkArgument(product != null, "Product must be defined");
        checkArgument(validity != null, "Validity must be defined");

        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.product = product;
        this.categories = categories != null ? Set.copyOf(categories) : Set.of();
        this.validity = validity;
        this.metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
    }

    static Builder builder() {
        return new Builder();
    }

    CatalogEntryId id() {
        return id;
    }

    String displayName() {
        return displayName;
    }

    String description() {
        return description;
    }

    Product product() {
        return product;
    }

    Set<String> categories() {
        return categories;
    }

    Validity validity() {
        return validity;
    }

    Map<String, String> metadata() {
        return metadata;
    }

    /**
     * Checks if entry is available for purchase at given date.
     */
    boolean isAvailableAt(LocalDate date) {
        return validity.isValidAt(date);
    }

    /**
     * Checks if entry belongs to given category.
     */
    boolean isInCategory(String category) {
        return categories.contains(category);
    }

    /**
     * Returns metadata value for given key.
     */
    Optional<String> getMetadata(String key) {
        return Optional.ofNullable(metadata.get(key));
    }

    /**
     * Returns metadata value or default if not present.
     */
    String getMetadataOrDefault(String key, String defaultValue) {
        return metadata.getOrDefault(key, defaultValue);
    }

    /**
     * Checks if metadata key exists.
     */
    boolean hasMetadata(String key) {
        return metadata.containsKey(key);
    }

    /**
     * Creates a copy with updated validity (for discontinuation).
     */
    CatalogEntry withValidity(Validity newValidity) {
        return new CatalogEntry(
            id,
            displayName,
            description,
            product,
            categories,
            newValidity,
            metadata
        );
    }

    /**
     * Creates a copy with updated metadata.
     */
    CatalogEntry withMetadata(Map<String, String> newMetadata) {
        return new CatalogEntry(
            id,
            displayName,
            description,
            product,
            categories,
            validity,
            newMetadata
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CatalogEntry that = (CatalogEntry) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "CatalogEntry{id=%s, displayName='%s', product=%s, categories=%s, validity=%s}"
            .formatted(id, displayName, product.name(), categories, validity);
    }

    static class Builder {
        private CatalogEntryId id;
        private String displayName;
        private String description;
        private Product product;
        private Set<String> categories = new HashSet<>();
        private Validity validity;
        private Map<String, String> metadata = new HashMap<>();

        Builder id(CatalogEntryId id) {
            this.id = id;
            return this;
        }

        Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        Builder description(String description) {
            this.description = description;
            return this;
        }

        Builder product(Product product) {
            this.product = product;
            return this;
        }

        Builder categories(Set<String> categories) {
            this.categories = new HashSet<>(categories);
            return this;
        }

        Builder category(String category) {
            this.categories.add(category);
            return this;
        }

        Builder validity(Validity validity) {
            this.validity = validity;
            return this;
        }

        Builder metadata(Map<String, String> metadata) {
            this.metadata = new HashMap<>(metadata);
            return this;
        }

        Builder withMetadata(String key, String value) {
            this.metadata.put(key, value);
            return this;
        }

        CatalogEntry build() {
            return new CatalogEntry(
                id,
                displayName,
                description,
                product,
                categories,
                validity,
                metadata
            );
        }
    }
}
