package com.softwarearchetypes.product;

import java.util.ArrayList;
import java.util.List;

import com.softwarearchetypes.quantity.Unit;

import static com.softwarearchetypes.common.Preconditions.checkArgument;

/**
 * ProductType represents a type/definition of a product (not a specific instance).
 * Examples: "iPhone 15 Pro 256GB", "Clean Code book", "Organic Milk 1L"
 *
 * Each ProductType is uniquely identified by a ProductIdentifier (ISBN, GTIN, UUID, etc.)
 * and defines how instances should be tracked and measured.
 *
 * ProductType can define mandatory and optional feature types that instances must or may have.
 */
class ProductType {

    private final ProductIdentifier id;
    private final ProductName name;
    private final ProductDescription description;
    private final Unit preferredUnit;
    private final ProductTrackingStrategy trackingStrategy;
    private final ProductFeatureTypes featureTypes;

    ProductType(ProductIdentifier id,
                ProductName name,
                ProductDescription description,
                Unit preferredUnit,
                ProductTrackingStrategy trackingStrategy,
                ProductFeatureTypes featureTypes) {
        checkArgument(id != null, "ProductIdentifier must be defined");
        checkArgument(name != null, "ProductName must be defined");
        checkArgument(description != null, "ProductDescription must be defined");
        checkArgument(preferredUnit != null, "Unit must be defined");
        checkArgument(trackingStrategy != null, "ProductTrackingStrategy must be defined");
        checkArgument(featureTypes != null, "ProductFeatureTypes must be defined");
        this.id = id;
        this.name = name;
        this.description = description;
        this.preferredUnit = preferredUnit;
        this.trackingStrategy = trackingStrategy;
        this.featureTypes = featureTypes;
    }

    /**
     * Creates a unique product type (one-of-a-kind).
     * Unit is implicitly pieces, as there's always exactly 1 exemplar.
     */
    static ProductType unique(ProductIdentifier id,
                              ProductName name,
                              ProductDescription description) {
        return new ProductType(id, name, description, Unit.pieces(), ProductTrackingStrategy.UNIQUE, ProductFeatureTypes.empty());
    }

    /**
     * Creates a product type where each instance is individually tracked (e.g., by serial number).
     */
    static ProductType individuallyTracked(ProductIdentifier id,
                                          ProductName name,
                                          ProductDescription description,
                                          Unit preferredUnit) {
        return new ProductType(id, name, description, preferredUnit, ProductTrackingStrategy.INDIVIDUALLY_TRACKED, ProductFeatureTypes.empty());
    }

    /**
     * Creates a product type where instances are tracked by production batch.
     */
    static ProductType batchTracked(ProductIdentifier id,
                                   ProductName name,
                                   ProductDescription description,
                                   Unit preferredUnit) {
        return new ProductType(id, name, description, preferredUnit, ProductTrackingStrategy.BATCH_TRACKED, ProductFeatureTypes.empty());
    }

    /**
     * Creates a product type where instances are tracked both individually and by batch.
     */
    static ProductType individuallyAndBatchTracked(ProductIdentifier id,
                                                  ProductName name,
                                                  ProductDescription description,
                                                  Unit preferredUnit) {
        return new ProductType(id, name, description, preferredUnit, ProductTrackingStrategy.INDIVIDUALLY_AND_BATCH_TRACKED, ProductFeatureTypes.empty());
    }

    /**
     * Creates a product type where instances are interchangeable (identical).
     */
    static ProductType identical(ProductIdentifier id,
                                ProductName name,
                                ProductDescription description,
                                Unit preferredUnit) {
        return new ProductType(id, name, description, preferredUnit, ProductTrackingStrategy.IDENTICAL, ProductFeatureTypes.empty());
    }

    ProductIdentifier id() {
        return id;
    }

    ProductName name() {
        return name;
    }

    ProductDescription description() {
        return description;
    }

    Unit preferredUnit() {
        return preferredUnit;
    }

    ProductTrackingStrategy trackingStrategy() {
        return trackingStrategy;
    }

    ProductFeatureTypes featureTypes() {
        return featureTypes;
    }

    /**
     * Creates a builder for constructing ProductTypes with feature types.
     * All basic product attributes are required upfront.
     */
    static Builder builder(ProductIdentifier id,
                          ProductName name,
                          ProductDescription description,
                          Unit preferredUnit,
                          ProductTrackingStrategy trackingStrategy) {
        return new Builder(id, name, description, preferredUnit, trackingStrategy);
    }

    @Override
    public String toString() {
        return "ProductType{id=%s, name=%s, unit=%s, tracking=%s, features=%s}".formatted(
            id, name, preferredUnit, trackingStrategy, featureTypes
        );
    }

    static class Builder {
        private final ProductIdentifier id;
        private final ProductName name;
        private final ProductDescription description;
        private final Unit preferredUnit;
        private final ProductTrackingStrategy trackingStrategy;
        private final List<ProductFeatureTypeDefinition> featureDefinitions = new ArrayList<>();

        private Builder(ProductIdentifier id,
                       ProductName name,
                       ProductDescription description,
                       Unit preferredUnit,
                       ProductTrackingStrategy trackingStrategy) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.preferredUnit = preferredUnit;
            this.trackingStrategy = trackingStrategy;
        }

        Builder withMandatoryFeature(ProductFeatureType featureType) {
            this.featureDefinitions.add(ProductFeatureTypeDefinition.mandatory(featureType));
            return this;
        }

        Builder withOptionalFeature(ProductFeatureType featureType) {
            this.featureDefinitions.add(ProductFeatureTypeDefinition.optional(featureType));
            return this;
        }

        Builder withFeature(ProductFeatureTypeDefinition definition) {
            this.featureDefinitions.add(definition);
            return this;
        }

        ProductType build() {
            ProductFeatureTypes features = new ProductFeatureTypes(featureDefinitions);
            return new ProductType(id, name, description, preferredUnit, trackingStrategy, features);
        }
    }
}
