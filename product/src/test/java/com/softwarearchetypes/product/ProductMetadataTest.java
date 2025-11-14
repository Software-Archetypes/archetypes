package com.softwarearchetypes.product;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ProductMetadataTest {

    @Test
    void shouldCreateEmptyMetadata() {
        var metadata = ProductMetadata.empty();

        assertTrue(metadata.asMap().isEmpty());
        assertFalse(metadata.has("category"));
    }

    @Test
    void shouldCreateMetadataFromMap() {
        var metadata = ProductMetadata.of(Map.of(
                "category", "coffee",
                "seasonal", "false",
                "brand", "Starbucks"
        ));

        assertTrue(metadata.has("category"));
        assertEquals("coffee", metadata.get("category").orElseThrow());
        assertEquals("false", metadata.get("seasonal").orElseThrow());
        assertEquals("Starbucks", metadata.get("brand").orElseThrow());
    }

    @Test
    void shouldAddMetadataImmutably() {
        var metadata1 = ProductMetadata.empty();
        var metadata2 = metadata1.with("category", "coffee");
        var metadata3 = metadata2.with("seasonal", "true");

        // Original unchanged
        assertFalse(metadata1.has("category"));

        // Second has category
        assertTrue(metadata2.has("category"));
        assertFalse(metadata2.has("seasonal"));

        // Third has both
        assertTrue(metadata3.has("category"));
        assertTrue(metadata3.has("seasonal"));
    }

    @Test
    void shouldGetWithDefault() {
        var metadata = ProductMetadata.of(Map.of("category", "coffee"));

        assertEquals("coffee", metadata.getOrDefault("category", "unknown"));
        assertEquals("unknown", metadata.getOrDefault("brand", "unknown"));
    }

    @Test
    void shouldUseMetadataInProductType() {
        var productType = ProductType.builder(
                        UuidProductIdentifier.random(),
                        ProductName.of("Pumpkin Spice Latte"),
                        ProductDescription.of("Seasonal coffee"),
                        com.softwarearchetypes.quantity.Unit.pieces(),
                        ProductTrackingStrategy.IDENTICAL
                )
                .withMetadata("category", "coffee")
                .withMetadata("seasonal", "true")
                .withMetadata("season", "autumn")
                .build();

        assertEquals("coffee", productType.metadata().get("category").orElseThrow());
        assertEquals("true", productType.metadata().get("seasonal").orElseThrow());
        assertEquals("autumn", productType.metadata().get("season").orElseThrow());
    }
}
