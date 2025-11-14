package com.softwarearchetypes.product;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductRelationshipPolicyTest {

    private final InMemoryProductTypeRepository productTypeRepository = new InMemoryProductTypeRepository();

    @Test
    void shouldPreventSelfRelationship() {
        var policy = new NoSelfRelationshipPolicy();
        var productId = UuidProductIdentifier.random();

        boolean canDefine = policy.canDefineFor(productId, productId, ProductRelationshipType.COMPATIBLE_WITH);

        assertFalse(canDefine);
    }

    @Test
    void shouldAllowRelationshipBetweenDifferentProducts() {
        var policy = new NoSelfRelationshipPolicy();
        var product1 = UuidProductIdentifier.random();
        var product2 = UuidProductIdentifier.random();

        boolean canDefine = policy.canDefineFor(product1, product2, ProductRelationshipType.COMPATIBLE_WITH);

        assertTrue(canDefine);
    }

    @Test
    void shouldPreventCompatibilityBetweenSeasonalAndNonSeasonal() {
        var policy = new NoSeasonalCompatibilityPolicy(productTypeRepository);

        var pumpkinSpiceLatte = createSeasonalProduct();
        var regularLatte = createNonSeasonalProduct();

        boolean canDefine = policy.canDefineFor(
                pumpkinSpiceLatte.identifier(),
                regularLatte.identifier(),
                ProductRelationshipType.COMPATIBLE_WITH
        );

        assertFalse(canDefine);
    }

    @Test
    void shouldAllowCompatibilityBetweenBothSeasonal() {
        var policy = new NoSeasonalCompatibilityPolicy(productTypeRepository);

        var pumpkinSpiceLatte = createSeasonalProduct();
        var gingerbreadLatte = createSeasonalProduct();

        boolean canDefine = policy.canDefineFor(
                pumpkinSpiceLatte.identifier(),
                gingerbreadLatte.identifier(),
                ProductRelationshipType.COMPATIBLE_WITH
        );

        assertTrue(canDefine);
    }

    @Test
    void shouldAllowCompatibilityBetweenBothNonSeasonal() {
        var policy = new NoSeasonalCompatibilityPolicy(productTypeRepository);

        var regularLatte = createNonSeasonalProduct();
        var cappuccino = createNonSeasonalProduct();

        boolean canDefine = policy.canDefineFor(
                regularLatte.identifier(),
                cappuccino.identifier(),
                ProductRelationshipType.COMPATIBLE_WITH
        );

        assertTrue(canDefine);
    }

    @Test
    void shouldAllowNonCompatibilityRelationshipsBetweenSeasonalAndNonSeasonal() {
        var policy = new NoSeasonalCompatibilityPolicy(productTypeRepository);

        var pumpkinSpiceLatte = createSeasonalProduct();
        var regularLatte = createNonSeasonalProduct();

        // Policy only restricts COMPATIBLE_WITH, not other types
        boolean canDefineUpgrade = policy.canDefineFor(
                pumpkinSpiceLatte.identifier(),
                regularLatte.identifier(),
                ProductRelationshipType.UPGRADABLE_TO
        );

        assertTrue(canDefineUpgrade);
    }

    private ProductType createSeasonalProduct() {
        var product = ProductType.builder(
                        UuidProductIdentifier.random(),
                        ProductName.of("Seasonal Product"),
                        ProductDescription.of("Seasonal"),
                        com.softwarearchetypes.quantity.Unit.pieces(),
                        ProductTrackingStrategy.IDENTICAL
                )
                .withMetadata("seasonal", "true")
                .build();
        productTypeRepository.save(product);
        return product;
    }

    private ProductType createNonSeasonalProduct() {
        var product = ProductType.builder(
                        UuidProductIdentifier.random(),
                        ProductName.of("Non-Seasonal Product"),
                        ProductDescription.of("Regular"),
                        com.softwarearchetypes.quantity.Unit.pieces(),
                        ProductTrackingStrategy.IDENTICAL
                )
                .withMetadata("seasonal", "false")
                .build();
        productTypeRepository.save(product);
        return product;
    }
}

class NoSelfRelationshipPolicy implements ProductRelationshipDefiningPolicy {
    @Override
    public boolean canDefineFor(ProductIdentifier from, ProductIdentifier to, ProductRelationshipType type) {
        return !from.equals(to);
    }
}

class NoSeasonalCompatibilityPolicy implements ProductRelationshipDefiningPolicy {
    private final ProductTypeRepository productRepo;

    NoSeasonalCompatibilityPolicy(ProductTypeRepository productRepo) {
        this.productRepo = productRepo;
    }

    @Override
    public boolean canDefineFor(ProductIdentifier from, ProductIdentifier to, ProductRelationshipType type) {
        if (type != ProductRelationshipType.COMPATIBLE_WITH) {
            return true;
        }

        var fromProduct = productRepo.findBy(from).orElseThrow();
        var toProduct = productRepo.findBy(to).orElseThrow();

        boolean fromSeasonal = "true".equals(fromProduct.metadata().getOrDefault("seasonal", "false"));
        boolean toSeasonal = "true".equals(toProduct.metadata().getOrDefault("seasonal", "false"));

        return fromSeasonal == toSeasonal;
    }
}
