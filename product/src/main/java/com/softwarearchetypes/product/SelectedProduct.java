package com.softwarearchetypes.product;

import static com.softwarearchetypes.common.Preconditions.checkArgument;

/**
 * Represents a product selected by customer with quantity.
 * Used for validating package configuration against package structure rules.
 */
record SelectedProduct(ProductIdentifier productId, int quantity) {
    public SelectedProduct {
        checkArgument(productId != null, "ProductId must be defined");
        checkArgument(quantity > 0, "Quantity must be > 0");
    }
}
