package com.softwarearchetypes.product;

import static com.softwarearchetypes.common.Preconditions.checkArgument;

/**
 * SelectedInstance represents a concrete instance (ProductInstance or PackageInstance)
 * that was selected/delivered as part of a package.
 * <p>
 * Used in PackageInstance to track what was actually delivered.
 * <p>
 * Example: Customer ordered "Laptop Bundle" and received:
 * - SelectedInstance(laptopInstance with serial ABC123, quantity=1)
 * - SelectedInstance(ramInstance from batch LOT-2025, quantity=1)
 * - SelectedInstance(ssdInstance with serial XYZ789, quantity=1)
 */
record SelectedInstance(Instance instance, int quantity) {

    public SelectedInstance {
        checkArgument(instance != null, "Instance must be defined");
        checkArgument(quantity > 0, "Quantity must be > 0");
    }

    /**
     * Helper: get the Product (ProductType or PackageType) of this instance.
     */
    Product product() {
        return instance.product();
    }

    /**
     * Helper: get the ProductIdentifier of this instance's product.
     */
    ProductIdentifier productId() {
        return instance.product().id();
    }

    /**
     * Helper: get the InstanceId.
     */
    InstanceId instanceId() {
        return instance.id();
    }

    /**
     * Converts this SelectedInstance to SelectedProduct.
     * Useful for package validation where we need to check product types, not instances.
     */
    SelectedProduct toSelectedProduct() {
        return new SelectedProduct(instance.product().id(), quantity);
    }
}
