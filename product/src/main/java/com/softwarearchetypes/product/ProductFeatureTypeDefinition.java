package com.softwarearchetypes.product;

import static com.softwarearchetypes.common.Preconditions.checkArgument;

/**
 * Defines a product feature type along with whether it's mandatory or optional.
 */
class ProductFeatureTypeDefinition {

    private final ProductFeatureType featureType;
    private final boolean mandatory;

    ProductFeatureTypeDefinition(ProductFeatureType featureType, boolean mandatory) {
        checkArgument(featureType != null, "ProductFeatureType must be defined");
        this.featureType = featureType;
        this.mandatory = mandatory;
    }

    static ProductFeatureTypeDefinition mandatory(ProductFeatureType featureType) {
        return new ProductFeatureTypeDefinition(featureType, true);
    }

    static ProductFeatureTypeDefinition optional(ProductFeatureType featureType) {
        return new ProductFeatureTypeDefinition(featureType, false);
    }

    ProductFeatureType featureType() {
        return featureType;
    }

    boolean isMandatory() {
        return mandatory;
    }

    boolean isOptional() {
        return !mandatory;
    }

    @Override
    public String toString() {
        return "%s(%s)".formatted(mandatory ? "mandatory" : "optional", featureType.name());
    }
}
