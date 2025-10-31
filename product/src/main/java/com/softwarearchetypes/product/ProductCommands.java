package com.softwarearchetypes.product;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

/**
 * Public API - commands for interacting with ProductFacade and ProductCatalog.
 * Commands represent write operations (mutations).
 * All fields use simple types - no domain objects leak through the API.
 */
public class ProductCommands {

    private ProductCommands() {
        // Static utility class
    }

    // ============================================
    // Feature Constraint Configurations
    // ============================================

    /**
     * Base interface for feature constraint configurations.
     * Each implementation represents a specific type of constraint.
     */
    public sealed interface FeatureConstraintConfig
        permits AllowedValuesConfig, NumericRangeConfig, DecimalRangeConfig,
                RegexConfig, DateRangeConfig, UnconstrainedConfig {
    }

    public record AllowedValuesConfig(Set<String> allowedValues) implements FeatureConstraintConfig {
        public AllowedValuesConfig {
            if (allowedValues == null || allowedValues.isEmpty()) {
                throw new IllegalArgumentException("Allowed values must not be empty");
            }
        }
    }

    public record NumericRangeConfig(int min, int max) implements FeatureConstraintConfig {
        public NumericRangeConfig {
            if (min > max) throw new IllegalArgumentException("Min must be less than or equal to max");
        }
    }

    public record DecimalRangeConfig(String min, String max) implements FeatureConstraintConfig {
        public DecimalRangeConfig {
            if (min == null || max == null) throw new IllegalArgumentException("Min and max must be defined");
        }
    }

    public record RegexConfig(String pattern) implements FeatureConstraintConfig {
        public RegexConfig {
            if (pattern == null || pattern.isBlank()) throw new IllegalArgumentException("Pattern must be defined");
        }
    }

    public record DateRangeConfig(String from, String to) implements FeatureConstraintConfig {
        public DateRangeConfig {
            if (from == null || to == null) throw new IllegalArgumentException("From and to dates must be defined");
        }
    }

    public record UnconstrainedConfig(String valueType) implements FeatureConstraintConfig {
        public UnconstrainedConfig {
            if (valueType == null || valueType.isBlank()) throw new IllegalArgumentException("Value type must be defined");
        }
    }

    // ============================================
    // ProductFacade Commands
    // ============================================

    /**
     * Command to define a new ProductType in the system.
     */
    public record DefineProductType(
        String productIdType,          // "UUID", "ISBN", "GTIN"
        String productId,              // Will be parsed to ProductIdentifier based on productIdType
        String name,
        String description,
        String unit,                   // e.g., "pcs", "kg", "m²"
        String trackingStrategy,       // "IDENTICAL", "INDIVIDUALLY_TRACKED", "BATCH_TRACKED", etc.
        Set<MandatoryFeature> mandatoryFeatures,
        Set<OptionalFeature> optionalFeatures
    ) {
        public DefineProductType {
            if (productIdType == null || productIdType.isBlank()) throw new IllegalArgumentException("Product ID type must be defined");
            if (productId == null || productId.isBlank()) throw new IllegalArgumentException("Product ID must be defined");
            if (name == null || name.isBlank()) throw new IllegalArgumentException("Name must be defined");
            if (description == null || description.isBlank()) throw new IllegalArgumentException("Description must be defined");
            if (unit == null || unit.isBlank()) throw new IllegalArgumentException("Unit must be defined");
            if (trackingStrategy == null || trackingStrategy.isBlank()) throw new IllegalArgumentException("Tracking strategy must be defined");
        }
    }

    /**
     * Mandatory feature definition.
     */
    public record MandatoryFeature(
        String name,
        FeatureConstraintConfig constraint
    ) {
        public MandatoryFeature {
            if (name == null || name.isBlank()) throw new IllegalArgumentException("Feature name must be defined");
            if (constraint == null) throw new IllegalArgumentException("Constraint must be defined");
        }
    }

    /**
     * Optional feature definition.
     */
    public record OptionalFeature(
        String name,
        FeatureConstraintConfig constraint
    ) {
        public OptionalFeature {
            if (name == null || name.isBlank()) throw new IllegalArgumentException("Feature name must be defined");
            if (constraint == null) throw new IllegalArgumentException("Constraint must be defined");
        }
    }

    // ============================================
    // ProductCatalog Commands
    // ============================================

    /**
     * Command to add a ProductType to the commercial offer.
     */
    public record AddToOffer(
        String productTypeId,          // Will be parsed to ProductIdentifier
        String displayName,
        String description,
        Set<String> categories,
        LocalDate availableFrom,
        LocalDate availableUntil,
        Map<String, String> metadata
    ) {
        public AddToOffer {
            if (productTypeId == null || productTypeId.isBlank()) throw new IllegalArgumentException("Product type ID must be defined");
            if (displayName == null || displayName.isBlank()) throw new IllegalArgumentException("Display name must be defined");
            if (description == null || description.isBlank()) throw new IllegalArgumentException("Description must be defined");
        }
    }

    /**
     * Command to discontinue a product from the offer.
     */
    public record DiscontinueProduct(
        String catalogEntryId,         // Will be parsed to CatalogEntryId
        LocalDate discontinuationDate
    ) {
        public DiscontinueProduct {
            if (catalogEntryId == null || catalogEntryId.isBlank()) throw new IllegalArgumentException("Catalog entry ID must be defined");
            if (discontinuationDate == null) throw new IllegalArgumentException("Discontinuation date must be defined");
        }
    }

    /**
     * Command to update catalog entry metadata.
     */
    public record UpdateMetadata(
        String catalogEntryId,         // Will be parsed to CatalogEntryId
        Map<String, String> metadata
    ) {
        public UpdateMetadata {
            if (catalogEntryId == null || catalogEntryId.isBlank()) throw new IllegalArgumentException("Catalog entry ID must be defined");
            if (metadata == null) throw new IllegalArgumentException("Metadata must be defined");
        }
    }
}
