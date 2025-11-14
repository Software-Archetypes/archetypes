package com.softwarearchetypes.product;

import java.util.List;

import static com.softwarearchetypes.common.Preconditions.checkArgument;

/**
 * PackageType represents a product composed of other products.
 * <p>
 * Examples:
 * - Laptop Bundle (laptop + bag + mouse + insurance)
 * - Banking Package (account + card + insurance)
 * - Office Setup (hardware package + software package + support)
 * <p>
 * PackageType can contain other PackageTypes (nested packages), forming a composite structure.
 * This is the composite in the composite pattern.
 */
class PackageType implements Product {

    private final ProductIdentifier id;
    private final ProductName name;
    private final ProductDescription description;
    private final ProductTrackingStrategy trackingStrategy;
    private final ProductMetadata metadata;
    private final ApplicabilityConstraint applicabilityConstraint;
    private final PackageStructure structure;

    PackageType(ProductIdentifier id,
                ProductName name,
                ProductDescription description,
                ProductTrackingStrategy trackingStrategy,
                ProductMetadata metadata,
                ApplicabilityConstraint applicabilityConstraint,
                PackageStructure structure) {
        checkArgument(id != null, "ProductIdentifier must be defined");
        checkArgument(name != null, "ProductName must be defined");
        checkArgument(description != null, "ProductDescription must be defined");
        checkArgument(trackingStrategy != null, "ProductTrackingStrategy must be defined");
        checkArgument(metadata != null, "ProductMetadata must be defined");
        checkArgument(applicabilityConstraint != null, "ApplicabilityConstraint must be defined");
        checkArgument(structure != null, "PackageStructure must be defined");

        this.id = id;
        this.name = name;
        this.description = description;
        this.trackingStrategy = trackingStrategy;
        this.metadata = metadata;
        this.applicabilityConstraint = applicabilityConstraint;
        this.structure = structure;
    }

    /**
     * Creates a package with default settings (UNIQUE tracking, no applicability constraints).
     */
    static PackageType define(ProductIdentifier id,
                             ProductName name,
                             ProductDescription description,
                             PackageStructure structure) {
        return new PackageType(id, name, description,
            ProductTrackingStrategy.UNIQUE,
            ProductMetadata.empty(),
            ApplicabilityConstraint.alwaysTrue(),
            structure);
    }

    @Override
    public ProductIdentifier id() {
        return id;
    }

    @Override
    public ProductName name() {
        return name;
    }

    @Override
    public ProductDescription description() {
        return description;
    }

    @Override
    public ProductMetadata metadata() {
        return metadata;
    }

    @Override
    public ApplicabilityConstraint applicabilityConstraint() {
        return applicabilityConstraint;
    }

    ProductTrackingStrategy trackingStrategy() {
        return trackingStrategy;
    }

    PackageStructure structure() {
        return structure;
    }

    /**
     * Validates if selected products match package structure rules.
     */
    public PackageValidationResult validateSelection(List<SelectedProduct> selection) {
        return structure.validate(selection);
    }

    @Override
    public String toString() {
        return "PackageType{id=%s, name=%s, tracking=%s, structure=%s}".formatted(
            id, name, trackingStrategy, structure
        );
    }
}
