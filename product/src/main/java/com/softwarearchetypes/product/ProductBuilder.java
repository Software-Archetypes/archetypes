package com.softwarearchetypes.product;

import com.softwarearchetypes.quantity.Unit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Builder for creating both ProductType and PackageType with fluent API.
 * <p>
 * Common attributes (id, name, description, metadata, applicability) are set in the main builder.
 * Type-specific attributes are set in specialized inner builders returned by asProductType() or asPackage().
 * <p>
 * Usage:
 * <pre>
 * ProductType laptop = new ProductBuilder(id, name, description)
 *     .withMetadata("category", "electronics")
 *     .asProductType(Unit.pieces(), ProductTrackingStrategy.INDIVIDUALLY_TRACKED)
 *         .withMandatoryFeature(colorFeature)
 *         .build();
 *
 * PackageType bundle = new ProductBuilder(id, name, description)
 *     .withMetadata("promotion", "summer2025")
 *     .asPackage()
 *         .withSingleChoice("Memory", ram8GB.id(), ram16GB.id())
 *         .withSingleChoice("Storage", ssd256GB.id(), ssd512GB.id())
 *         .withOptionalChoice("Accessories", mouse.id(), bag.id())
 *         .build();
 * </pre>
 */
class ProductBuilder {
    // Common fields - required upfront
    private final ProductIdentifier id;
    private final ProductName name;
    private final ProductDescription description;

    // Common fields - optional with defaults
    private ProductMetadata metadata = ProductMetadata.empty();
    private ApplicabilityConstraint applicabilityConstraint = ApplicabilityConstraint.alwaysTrue();

    ProductBuilder(ProductIdentifier id, ProductName name, ProductDescription description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    /**
     * Sets metadata for the product/package.
     */
    public ProductBuilder withMetadata(ProductMetadata metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Adds a single metadata entry.
     */
    public ProductBuilder withMetadata(String key, String value) {
        this.metadata = this.metadata.with(key, value);
        return this;
    }

    /**
     * Sets applicability constraint for the product/package.
     */
    public ProductBuilder withApplicabilityConstraint(ApplicabilityConstraint constraint) {
        this.applicabilityConstraint = constraint;
        return this;
    }

    /**
     * Starts building a ProductType (regular product).
     * Returns specialized builder for ProductType-specific attributes.
     */
    public ProductTypeBuilder asProductType(Unit preferredUnit,
                                            ProductTrackingStrategy trackingStrategy) {
        return new ProductTypeBuilder(preferredUnit, trackingStrategy);
    }

    /**
     * Starts building a PackageType (package of products).
     * Returns specialized builder with fluent API for defining package structure.
     */
    public PackageTypeBuilder asPackage() {
        return new PackageTypeBuilder();
    }

    /**
     * Specialized builder for ProductType.
     * Has access to common fields from outer ProductBuilder.
     */
    public class ProductTypeBuilder {
        private final Unit preferredUnit;
        private final ProductTrackingStrategy trackingStrategy;
        private final List<ProductFeatureTypeDefinition> featureDefinitions = new ArrayList<>();

        ProductTypeBuilder(Unit preferredUnit, ProductTrackingStrategy trackingStrategy) {
            this.preferredUnit = preferredUnit;
            this.trackingStrategy = trackingStrategy;
        }

        public ProductTypeBuilder withMandatoryFeature(ProductFeatureType featureType) {
            this.featureDefinitions.add(ProductFeatureTypeDefinition.mandatory(featureType));
            return this;
        }

        public ProductTypeBuilder withOptionalFeature(ProductFeatureType featureType) {
            this.featureDefinitions.add(ProductFeatureTypeDefinition.optional(featureType));
            return this;
        }

        public ProductTypeBuilder withFeature(ProductFeatureTypeDefinition definition) {
            this.featureDefinitions.add(definition);
            return this;
        }

        public ProductType build() {
            ProductFeatureTypes features = new ProductFeatureTypes(featureDefinitions);
            return new ProductType(id, name, description, preferredUnit, trackingStrategy,
                features, metadata, applicabilityConstraint);
        }
    }

    /**
     * Specialized builder for PackageType with fluent API for defining package structure.
     * Has access to common fields from outer ProductBuilder.
     */
    public class PackageTypeBuilder {
        private final Map<String, ProductSet> productSets = new HashMap<>();
        private final List<SelectionRule> selectionRules = new ArrayList<>();
        private ProductTrackingStrategy trackingStrategy = ProductTrackingStrategy.UNIQUE;

        PackageTypeBuilder() {
        }

        public PackageTypeBuilder withTrackingStrategy(ProductTrackingStrategy trackingStrategy) {
            this.trackingStrategy = trackingStrategy;
            return this;
        }

        /**
         * Adds a product set with "select exactly one" rule.
         * Example: customer must choose exactly 1 memory option from the set.
         */
        public PackageTypeBuilder withSingleChoice(String setName, ProductIdentifier... productIds) {
            return withChoice(setName, 1, 1, productIds);
        }

        /**
         * Adds a product set with "select zero or one" rule.
         * Example: customer may optionally choose 1 accessory from the set.
         */
        public PackageTypeBuilder withOptionalChoice(String setName, ProductIdentifier... productIds) {
            return withChoice(setName, 0, 1, productIds);
        }

        /**
         * Adds a product set with "select at least one" rule.
         * Example: customer must choose at least 1 item from the set (can choose more).
         */
        public PackageTypeBuilder withRequiredChoice(String setName, ProductIdentifier... productIds) {
            return withChoice(setName, 1, Integer.MAX_VALUE, productIds);
        }

        /**
         * Adds a product set with custom min/max selection rule.
         * Example: customer must choose 2-4 items from the set.
         */
        public PackageTypeBuilder withChoice(String setName, int min, int max, ProductIdentifier... productIds) {
            ProductSet set = new ProductSet(setName, Set.of(productIds));
            productSets.put(setName, set);
            selectionRules.add(SelectionRule.isSubsetOf(set, min, max));
            return this;
        }

        /**
         * Adds a custom selection rule for advanced cases (conditional rules, AND/OR composition).
         * Use this when simple choice rules are not enough.
         */
        public PackageTypeBuilder withSelectionRule(SelectionRule rule) {
            selectionRules.add(rule);
            return this;
        }

        /**
         * Returns the current ProductSet by name for use in conditional rules.
         * Example:
         * <pre>
         * builder.withSingleChoice("Laptops", gaming, business)
         *        .withConditional()
         *            .ifSelected("Laptops", gaming.id())
         *            .thenRequired("Graphics", dedicatedGPU.id())
         * </pre>
         */
        public ProductSet getProductSet(String setName) {
            return productSets.get(setName);
        }

        public PackageType build() {
            PackageStructure structure = new PackageStructure(productSets, selectionRules);
            return new PackageType(id, name, description, trackingStrategy,
                metadata, applicabilityConstraint, structure);
        }
    }
}
