package com.softwarearchetypes.product;

import static com.softwarearchetypes.common.Preconditions.checkArgument;

record ProductName(String value) {

    ProductName {
        checkArgument(value != null && !value.isBlank(), "ProductName cannot be null or blank");
    }

    static ProductName of(String value) {
        return new ProductName(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
