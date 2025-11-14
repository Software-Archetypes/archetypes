package com.softwarearchetypes.product;

import java.util.Set;

import static com.softwarearchetypes.common.Preconditions.checkArgument;

/**
 * ProductSet represents a named collection of products available for selection in a package.
 * This is the "raw material" - a pool of product options without any selection constraints.
 * <p>
 * Example: A laptop package might have ProductSets like "Memory Options" (4GB, 8GB, 16GB),
 * "Storage Options" (256GB SSD, 512GB SSD, 1TB SSD), "Accessories" (mouse, keyboard, bag).
 */
class ProductSet {
    private final String name;
    private final Set<ProductIdentifier> products;

    ProductSet(String name, Set<ProductIdentifier> products) {
        checkArgument(name != null && !name.isBlank(), "ProductSet name must be defined");
        checkArgument(products != null && !products.isEmpty(), "ProductSet must contain at least one product");
        this.name = name;
        this.products = Set.copyOf(products);
    }

    String name() {
        return name;
    }

    Set<ProductIdentifier> products() {
        return products;
    }

    boolean contains(ProductIdentifier productId) {
        return products.contains(productId);
    }

    @Override
    public String toString() {
        return "ProductSet{name='%s', products=%s}".formatted(name, products);
    }
}
