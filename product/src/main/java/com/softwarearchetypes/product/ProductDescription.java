package com.softwarearchetypes.product;

import static com.softwarearchetypes.common.Preconditions.checkArgument;

record ProductDescription(String value) {

    ProductDescription {
        checkArgument(value != null && !value.isBlank(), "ProductDescription cannot be null or blank");
    }

    static ProductDescription of(String value) {
        return new ProductDescription(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
