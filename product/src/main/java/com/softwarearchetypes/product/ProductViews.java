package com.softwarearchetypes.product;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

/**
 * Public API - view models (DTOs) returned by ProductFacade and ProductCatalog.
 * All fields use simple types - no domain objects leak through the API.
 */
public class ProductViews {

    private ProductViews() {
        // Static utility class
    }

    /**
     * View of a ProductType - business definition of a product.
     */
    public record ProductTypeView(
        String productId,
        String name,
        String description,
        String unit,
        String trackingStrategy,
        Set<FeatureTypeView> mandatoryFeatures,
        Set<FeatureTypeView> optionalFeatures
    ) {}

    /**
     * View of a ProductFeatureType - definition of a configurable characteristic.
     */
    public record FeatureTypeView(
        String name,
        String valueType,              // "TEXT", "INTEGER", "DECIMAL", "DATE", "BOOLEAN"
        String constraintType,         // "ALLOWED_VALUES", "NUMERIC_RANGE", "REGEX", etc.
        Map<String, Object> constraintConfig,
        String constraintDescription
    ) {}

    /**
     * View of a CatalogEntry - commercial offering position.
     */
    public record CatalogEntryView(
        String catalogEntryId,
        String displayName,
        String description,
        String productTypeId,
        Set<String> categories,
        LocalDate availableFrom,
        LocalDate availableUntil,
        Map<String, String> metadata
    ) {}

    /**
     * Metadata for a catalog entry or product type.
     * Generic key-value view for flexible attributes.
     */
    public record MetadataView(
        Map<String, String> attributes
    ) {
        public String get(String key) {
            return attributes.get(key);
        }

        public String getOrDefault(String key, String defaultValue) {
            return attributes.getOrDefault(key, defaultValue);
        }

        public boolean has(String key) {
            return attributes.containsKey(key);
        }
    }
}
