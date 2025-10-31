package com.softwarearchetypes.product;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

/**
 * Public API - queries for reading from ProductFacade and ProductCatalog.
 * Queries represent read operations (lookups, searches).
 * All fields use simple types - no domain objects leak through the API.
 */
public class ProductQueries {

    private ProductQueries() {
        // Static utility class
    }

    // ============================================
    // ProductFacade Queries
    // ============================================

    /**
     * Criteria to find a ProductType by its identifier.
     */
    public record FindProductTypeCriteria(
        String productId               // Will be parsed to ProductIdentifier
    ) {
        public FindProductTypeCriteria {
            if (productId == null || productId.isBlank()) throw new IllegalArgumentException("Product ID must be defined");
        }
    }

    /**
     * Criteria to find all ProductTypes matching given tracking strategy.
     */
    public record FindByTrackingStrategyCriteria(
        String trackingStrategy        // "IDENTICAL", "INDIVIDUALLY_TRACKED", "BATCH_TRACKED", etc.
    ) {
        public FindByTrackingStrategyCriteria {
            if (trackingStrategy == null || trackingStrategy.isBlank()) throw new IllegalArgumentException("Tracking strategy must be defined");
        }
    }

    // ============================================
    // ProductCatalog Queries
    // ============================================

    /**
     * Criteria to search catalog entries.
     * All filters are optional - null/empty values are ignored.
     */
    public record SearchCatalogCriteria(
        String searchText,             // Search in displayName and description
        Set<String> categories,        // Filter by categories (any match)
        LocalDate availableAt,         // Filter by availability at specific date
        String productTypeId,          // Filter by specific ProductType
        Map<String, Set<String>> productTypeFeatures  // Filter by ProductType features and their possible values
                                                       // Example: {"storage": ["256GB", "512GB"], "color": ["Blue"]}
                                                       // Matches if ProductType has feature "storage" that accepts "256GB" OR "512GB"
    ) {
        public SearchCatalogCriteria {
            // All parameters optional
        }

        /**
         * Creates criteria for full catalog listing (no filters).
         */
        public static SearchCatalogCriteria all() {
            return new SearchCatalogCriteria(null, null, null, null, null);
        }

        /**
         * Creates criteria for text search only.
         */
        public static SearchCatalogCriteria byText(String searchText) {
            return new SearchCatalogCriteria(searchText, null, null, null, null);
        }

        /**
         * Creates criteria for category filter only.
         */
        public static SearchCatalogCriteria byCategories(Set<String> categories) {
            return new SearchCatalogCriteria(null, categories, null, null, null);
        }

        /**
         * Creates criteria for availability at specific date.
         */
        public static SearchCatalogCriteria availableAt(LocalDate date) {
            return new SearchCatalogCriteria(null, null, date, null, null);
        }

        /**
         * Creates criteria for specific product type.
         */
        public static SearchCatalogCriteria byProductType(String productTypeId) {
            return new SearchCatalogCriteria(null, null, null, productTypeId, null);
        }

        /**
         * Creates criteria for product type features.
         * Example: byFeatures(Map.of("storage", Set.of("256GB", "512GB")))
         * finds all catalog entries where ProductType has feature "storage" accepting "256GB" or "512GB".
         */
        public static SearchCatalogCriteria byFeatures(Map<String, Set<String>> features) {
            return new SearchCatalogCriteria(null, null, null, null, features);
        }
    }

    /**
     * Criteria to find a catalog entry by its identifier.
     */
    public record FindCatalogEntryCriteria(
        String catalogEntryId          // Will be parsed to CatalogEntryId
    ) {
        public FindCatalogEntryCriteria {
            if (catalogEntryId == null || catalogEntryId.isBlank()) throw new IllegalArgumentException("Catalog entry ID must be defined");
        }
    }

    /**
     * Criteria to find catalog entries by category.
     */
    public record FindByCategoryCriteria(
        String category
    ) {
        public FindByCategoryCriteria {
            if (category == null || category.isBlank()) throw new IllegalArgumentException("Category must be defined");
        }
    }

    /**
     * Criteria to find catalog entries available at specific date.
     */
    public record FindAvailableAtCriteria(
        LocalDate date
    ) {
        public FindAvailableAtCriteria {
            if (date == null) throw new IllegalArgumentException("Date must be defined");
        }
    }

    /**
     * Criteria to find catalog entries by metadata key-value.
     */
    public record FindByMetadataCriteria(
        String key,
        String value
    ) {
        public FindByMetadataCriteria {
            if (key == null || key.isBlank()) throw new IllegalArgumentException("Metadata key must be defined");
        }
    }
}
