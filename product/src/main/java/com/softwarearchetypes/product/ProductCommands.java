package com.softwarearchetypes.product;

import com.softwarearchetypes.common.Preconditions;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import static com.softwarearchetypes.common.Preconditions.checkArgument;
import static com.softwarearchetypes.common.Preconditions.checkNotNull;

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
            checkArgument(allowedValues != null && !allowedValues.isEmpty(), "Allowed values must not be empty");
        }
    }

    public record NumericRangeConfig(int min, int max) implements FeatureConstraintConfig {
        public NumericRangeConfig {
            checkArgument(min <= max, "Min must be less than or equal to max");
        }
    }

    public record DecimalRangeConfig(String min, String max) implements FeatureConstraintConfig {
        public DecimalRangeConfig {
            checkNotNull(min, "Min must be defined");
            checkNotNull(max, "Max must be defined");
        }
    }

    public record RegexConfig(String pattern) implements FeatureConstraintConfig {
        public RegexConfig {
            checkArgument(pattern != null && !pattern.isBlank(), "Pattern must be defined");
        }
    }

    public record DateRangeConfig(String from, String to) implements FeatureConstraintConfig {
        public DateRangeConfig {
            checkNotNull(from, "From date must be defined");
            checkNotNull(to, "To date must be defined");
        }
    }

    public record UnconstrainedConfig(String valueType) implements FeatureConstraintConfig {
        public UnconstrainedConfig {
            checkArgument(valueType != null && !valueType.isBlank(), "Value type must be defined");
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
        Set<OptionalFeature> optionalFeatures,
        Map<String, String> metadata   // Static properties: category, seasonal, brand, etc.
    ) {
        public DefineProductType {
            checkArgument(productIdType != null && !productIdType.isBlank(), "Product ID type must be defined");
            checkArgument(productId != null && !productId.isBlank(), "Product ID must be defined");
            checkArgument(name != null && !name.isBlank(), "Name must be defined");
            checkArgument(description != null && !description.isBlank(), "Description must be defined");
            checkArgument(unit != null && !unit.isBlank(), "Unit must be defined");
            checkArgument(trackingStrategy != null && !trackingStrategy.isBlank(), "Tracking strategy must be defined");
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
            checkArgument(name != null && !name.isBlank(), "Feature name must be defined");
            checkNotNull(constraint, "Constraint must be defined");
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
            checkArgument(name != null && !name.isBlank(), "Feature name must be defined");
            checkNotNull(constraint, "Constraint must be defined");
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
            checkArgument(productTypeId != null && !productTypeId.isBlank(), "Product type ID must be defined");
            checkArgument(displayName != null && !displayName.isBlank(), "Display name must be defined");
            checkArgument(description != null && !description.isBlank(), "Description must be defined");
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
            checkArgument(catalogEntryId != null && !catalogEntryId.isBlank(), "Catalog entry ID must be defined");
            checkNotNull(discontinuationDate, "Discontinuation date must be defined");
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
            checkArgument(catalogEntryId != null && !catalogEntryId.isBlank(), "Catalog entry ID must be defined");
            checkNotNull(metadata, "Metadata must be defined");
        }
    }

    // ============================================
    // Package Commands
    // ============================================

    /**
     * Command to define a new PackageType in the system.
     */
    public record DefinePackageType(
        String productIdType,          // "UUID", "ISBN", "GTIN"
        String productId,              // Will be parsed to ProductIdentifier based on productIdType
        String name,
        String description,
        String unit,                   // e.g., "pcs", "kg"
        String trackingStrategy,       // "IDENTICAL", "INDIVIDUALLY_TRACKED", "BATCH_TRACKED", etc.
        Set<SelectionRuleConfig> selectionRules,
        Map<String, String> metadata   // Static properties: category, seasonal, etc.
    ) {
        public DefinePackageType {
            checkArgument(productIdType != null && !productIdType.isBlank(), "Product ID type must be defined");
            checkArgument(productId != null && !productId.isBlank(), "Product ID must be defined");
            checkArgument(name != null && !name.isBlank(), "Name must be defined");
            checkArgument(description != null && !description.isBlank(), "Description must be defined");
            checkArgument(unit != null && !unit.isBlank(), "Unit must be defined");
            checkArgument(trackingStrategy != null && !trackingStrategy.isBlank(), "Tracking strategy must be defined");
            checkArgument(selectionRules != null && !selectionRules.isEmpty(), "Selection rules must be defined");
        }
    }

    /**
     * Base interface for selection rule configurations.
     * Each implementation represents a specific type of rule.
     */
    public sealed interface SelectionRuleConfig
        permits IsSubsetOfConfig, SingleConfig, OptionalConfig, RequiredConfig,
                AndRuleConfig, OrRuleConfig, NotRuleConfig, IfThenRuleConfig {
    }

    /**
     * IsSubsetOf rule: select min to max products from a set.
     * Example: Select 2-3 accessories from {mouse, keyboard, monitor}.
     */
    public record IsSubsetOfConfig(
        Set<String> productIds,        // Product identifiers in the set
        int min,
        int max
    ) implements SelectionRuleConfig {
        public IsSubsetOfConfig {
            checkArgument(productIds != null && !productIds.isEmpty(), "Product IDs must not be empty");
            checkArgument(min >= 0, "Min must be >= 0");
            checkArgument(max >= min, "Max must be >= min");
        }
    }

    /**
     * Single rule: exactly one product from the set.
     * Shorthand for IsSubsetOf(set, 1, 1).
     */
    public record SingleConfig(
        Set<String> productIds         // Product identifiers in the set
    ) implements SelectionRuleConfig {
        public SingleConfig {
            checkArgument(productIds != null && !productIds.isEmpty(), "Product IDs must not be empty");
        }
    }

    /**
     * Optional rule: zero or one product from the set.
     * Shorthand for IsSubsetOf(set, 0, 1).
     */
    public record OptionalConfig(
        Set<String> productIds         // Product identifiers in the set
    ) implements SelectionRuleConfig {
        public OptionalConfig {
            checkArgument(productIds != null && !productIds.isEmpty(), "Product IDs must not be empty");
        }
    }

    /**
     * Required rule: at least one product from the set.
     * Shorthand for IsSubsetOf(set, 1, Integer.MAX_VALUE).
     */
    public record RequiredConfig(
        Set<String> productIds         // Product identifiers in the set
    ) implements SelectionRuleConfig {
        public RequiredConfig {
            checkArgument(productIds != null && !productIds.isEmpty(), "Product IDs must not be empty");
        }
    }

    /**
     * AND rule: all nested rules must be satisfied.
     */
    public record AndRuleConfig(
        Set<SelectionRuleConfig> rules
    ) implements SelectionRuleConfig {
        public AndRuleConfig {
            checkArgument(rules != null && !rules.isEmpty(), "Rules must not be empty");
        }
    }

    /**
     * OR rule: at least one nested rule must be satisfied.
     */
    public record OrRuleConfig(
        Set<SelectionRuleConfig> rules
    ) implements SelectionRuleConfig {
        public OrRuleConfig {
            checkArgument(rules != null && !rules.isEmpty(), "Rules must not be empty");
        }
    }

    /**
     * NOT rule: inverts the result of the nested rule.
     */
    public record NotRuleConfig(
        SelectionRuleConfig rule
    ) implements SelectionRuleConfig {
        public NotRuleConfig {
            checkNotNull(rule, "Rule must be defined");
        }
    }

    /**
     * IF-THEN rule: if condition is met, then all 'then' rules must be satisfied.
     */
    public record IfThenRuleConfig(
        SelectionRuleConfig condition,
        Set<SelectionRuleConfig> thenRules
    ) implements SelectionRuleConfig {
        public IfThenRuleConfig {
            checkNotNull(condition, "Condition must be defined");
            checkArgument(thenRules != null && !thenRules.isEmpty(), "Then rules must not be empty");
        }
    }

    // ============================================
    // Instance Commands
    // ============================================

    /**
     * Command to create a ProductInstance.
     */
    public record CreateProductInstance(
        String productTypeId,          // Product type this instance belongs to
        String serialNumber,           // Optional - can be null
        String batchId,                // Optional - can be null
        String quantity,               // e.g., "1", "5.5", "3.2"
        String unit,                   // e.g., "pcs", "kg", "l"
        Set<FeatureInstanceConfig> features
    ) {
        public CreateProductInstance {
            checkArgument(productTypeId != null && !productTypeId.isBlank(), "Product type ID must be defined");
            checkArgument(quantity != null && !quantity.isBlank(), "Quantity must be defined");
            checkArgument(unit != null && !unit.isBlank(), "Unit must be defined");
        }
    }

    /**
     * Feature instance configuration.
     */
    public record FeatureInstanceConfig(
        String featureName,
        String value
    ) {
        public FeatureInstanceConfig {
            checkArgument(featureName != null && !featureName.isBlank(), "Feature name must be defined");
            checkNotNull(value, "Feature value must be defined");
        }
    }

    /**
     * Command to create a PackageInstance.
     */
    public record CreatePackageInstance(
        String packageTypeId,          // Package type this instance belongs to
        String serialNumber,           // Optional - can be null
        String batchId,                // Optional - can be null
        Set<SelectedInstanceConfig> selection
    ) {
        public CreatePackageInstance {
            checkArgument(packageTypeId != null && !packageTypeId.isBlank(), "Package type ID must be defined");
            checkArgument(selection != null && !selection.isEmpty(), "Selection must not be empty");
        }
    }

    /**
     * Selected instance configuration (for PackageInstance).
     */
    public record SelectedInstanceConfig(
        String instanceId,             // ID of ProductInstance or PackageInstance
        int quantity
    ) {
        public SelectedInstanceConfig {
            checkArgument(instanceId != null && !instanceId.isBlank(), "Instance ID must be defined");
            checkArgument(quantity > 0, "Quantity must be > 0");
        }
    }
}
