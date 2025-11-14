package com.softwarearchetypes.product;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * ProductMetadata represents static, unchanging properties of a ProductType.
 * These are simple key-value pairs that describe fixed characteristics of the product type.
 *
 * Examples:
 * - category: "coffee", "electronics", "clothing"
 * - seasonal: "true", "false"
 * - season: "winter", "summer"
 * - brand: "Apple", "Samsung"
 * - material: "cotton", "polyester"
 */
public class ProductMetadata {

    private final Map<String, String> data;

    private ProductMetadata(Map<String, String> data) {
        this.data = Map.copyOf(data);
    }

    public static ProductMetadata empty() {
        return new ProductMetadata(Map.of());
    }

    public static ProductMetadata of(Map<String, String> data) {
        return new ProductMetadata(data != null ? data : Map.of());
    }

    public Optional<String> get(String key) {
        return Optional.ofNullable(data.get(key));
    }

    public String getOrDefault(String key, String defaultValue) {
        return data.getOrDefault(key, defaultValue);
    }

    public boolean has(String key) {
        return data.containsKey(key);
    }

    public Map<String, String> asMap() {
        return data;
    }

    public ProductMetadata with(String key, String value) {
        Map<String, String> newData = new HashMap<>(data);
        newData.put(key, value);
        return new ProductMetadata(newData);
    }

    @Override
    public String toString() {
        return "ProductMetadata" + data;
    }
}
