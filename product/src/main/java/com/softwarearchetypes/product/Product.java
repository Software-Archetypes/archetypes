package com.softwarearchetypes.product;

/**
 * Product - component in composite pattern.
 * Can be either ProductType (leaf - regular product) or PackageType (composite - package of products).
 */
interface Product {
    ProductIdentifier id();

    ProductName name();

    ProductDescription description();

    ProductMetadata metadata();

    ApplicabilityConstraint applicabilityConstraint();

    default boolean isApplicableFor(ApplicabilityContext context) {
        return applicabilityConstraint().isSatisfiedBy(context);
    }

    default ProductBuilder builder(ProductIdentifier id, ProductName name, ProductDescription description) {
        return new ProductBuilder(id, name, description);
    }
}
