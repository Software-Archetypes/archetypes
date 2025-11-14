package com.softwarearchetypes.product;

import com.softwarearchetypes.quantity.Quantity;
import com.softwarearchetypes.quantity.Unit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for InstanceBuilder and the unified building approach for ProductInstance and PackageInstance.
 */
class InstanceBuilderTest {

    private ProductType laptop;
    private ProductType mouse;
    private PackageType bundle;
    private ProductFeatureType colorFeature;

    @BeforeEach
    void setUp() {
        laptop = ProductType.builder(
                UuidProductIdentifier.random(),
                ProductName.of("Business Laptop"),
                ProductDescription.of("Professional laptop"),
                Unit.pieces(),
                ProductTrackingStrategy.INDIVIDUALLY_TRACKED
        ).build();

        mouse = ProductType.builder(
                UuidProductIdentifier.random(),
                ProductName.of("Wireless Mouse"),
                ProductDescription.of("Ergonomic mouse"),
                Unit.pieces(),
                ProductTrackingStrategy.TRACKED_BY_BATCH
        ).build();

        bundle = ProductBuilder.builder()
                .asPackageType(
                        UuidProductIdentifier.random(),
                        ProductName.of("Workstation Bundle"),
                        ProductDescription.of("Complete setup"),
                        Unit.pieces(),
                        ProductTrackingStrategy.INDIVIDUALLY_TRACKED
                )
                .withSelectionRule(SelectionRule.and(
                        SelectionRule.required(ProductSet.of(laptop.id())),
                        SelectionRule.required(ProductSet.of(mouse.id()))
                ))
                .build();

        colorFeature = ProductFeatureType.of(
                ProductFeatureName.of("Color"),
                ProductFeatureDescription.of("Device color")
        );
    }

    @Test
    void shouldBuildProductInstanceWithSerial() {
        InstanceId id = InstanceId.newOne();
        SerialNumber serial = SerialNumber.of("LAPTOP-123");

        ProductInstance instance = new InstanceBuilder(id)
                .withSerial(serial)
                .asProductInstance(laptop)
                .withQuantity(Quantity.of(1, Unit.pieces()))
                .build();

        assertNotNull(instance);
        assertEquals(id, instance.id());
        assertEquals(laptop, instance.product());
        assertTrue(instance.serialNumber().isPresent());
        assertEquals(serial, instance.serialNumber().get());
        assertFalse(instance.batchId().isPresent());
    }

    @Test
    void shouldBuildProductInstanceWithBatch() {
        InstanceId id = InstanceId.newOne();
        BatchId batch = BatchId.of("MOUSE-BATCH-001");

        ProductInstance instance = new InstanceBuilder(id)
                .withBatch(batch)
                .asProductInstance(mouse)
                .withQuantity(Quantity.of(1, Unit.pieces()))
                .build();

        assertNotNull(instance);
        assertEquals(id, instance.id());
        assertEquals(mouse, instance.product());
        assertFalse(instance.serialNumber().isPresent());
        assertTrue(instance.batchId().isPresent());
        assertEquals(batch, instance.batchId().get());
    }

    @Test
    void shouldBuildProductInstanceWithBothSerialAndBatch() {
        InstanceId id = InstanceId.newOne();
        SerialNumber serial = SerialNumber.of("LAPTOP-123");
        BatchId batch = BatchId.of("BATCH-2025-Q1");

        ProductInstance instance = new InstanceBuilder(id)
                .withSerial(serial)
                .withBatch(batch)
                .asProductInstance(laptop)
                .withQuantity(Quantity.of(1, Unit.pieces()))
                .build();

        assertNotNull(instance);
        assertTrue(instance.serialNumber().isPresent());
        assertTrue(instance.batchId().isPresent());
        assertEquals(serial, instance.serialNumber().get());
        assertEquals(batch, instance.batchId().get());
    }

    @Test
    void shouldBuildProductInstanceWithFeatures() {
        ProductInstance instance = new InstanceBuilder(InstanceId.newOne())
                .withSerial(SerialNumber.of("LAPTOP-123"))
                .asProductInstance(laptop)
                .withQuantity(Quantity.of(1, Unit.pieces()))
                .withFeature(colorFeature, "Silver")
                .build();

        assertNotNull(instance);
        assertEquals(1, instance.features().size());
        assertTrue(instance.features().has(colorFeature));
        assertEquals("Silver", instance.features().get(colorFeature).orElseThrow().value());
    }

    @Test
    void shouldBuildProductInstanceWithMultipleFeatures() {
        ProductFeatureType storageFeature = ProductFeatureType.of(
                ProductFeatureName.of("Storage"),
                ProductFeatureDescription.of("Storage capacity")
        );

        ProductInstance instance = new InstanceBuilder(InstanceId.newOne())
                .withSerial(SerialNumber.of("LAPTOP-123"))
                .asProductInstance(laptop)
                .withQuantity(Quantity.of(1, Unit.pieces()))
                .withFeature(colorFeature, "Silver")
                .withFeature(storageFeature, "512GB")
                .build();

        assertNotNull(instance);
        assertEquals(2, instance.features().size());
        assertTrue(instance.features().has(colorFeature));
        assertTrue(instance.features().has(storageFeature));
    }

    @Test
    void shouldBuildProductInstanceWithFeatureInstance() {
        ProductFeatureInstance featureInstance = new ProductFeatureInstance(colorFeature, "Black");

        ProductInstance instance = new InstanceBuilder(InstanceId.newOne())
                .withSerial(SerialNumber.of("LAPTOP-123"))
                .asProductInstance(laptop)
                .withQuantity(Quantity.of(1, Unit.pieces()))
                .withFeature(featureInstance)
                .build();

        assertNotNull(instance);
        assertEquals(1, instance.features().size());
        assertEquals("Black", instance.features().get(colorFeature).orElseThrow().value());
    }

    @Test
    void shouldBuildPackageInstanceWithSerial() {
        ProductInstance laptopInstance = new InstanceBuilder(InstanceId.newOne())
                .withSerial(SerialNumber.of("LAPTOP-001"))
                .asProductInstance(laptop)
                .withQuantity(Quantity.of(1, Unit.pieces()))
                .build();

        ProductInstance mouseInstance = new InstanceBuilder(InstanceId.newOne())
                .withBatch(BatchId.of("MOUSE-BATCH-001"))
                .asProductInstance(mouse)
                .withQuantity(Quantity.of(1, Unit.pieces()))
                .build();

        InstanceId packageId = InstanceId.newOne();
        SerialNumber packageSerial = SerialNumber.of("BUNDLE-001");

        PackageInstance packageInstance = new InstanceBuilder(packageId)
                .withSerial(packageSerial)
                .asPackageInstance(bundle)
                .withSelection(List.of(
                        new SelectedInstance(laptopInstance, 1),
                        new SelectedInstance(mouseInstance, 1)
                ))
                .build();

        assertNotNull(packageInstance);
        assertEquals(packageId, packageInstance.id());
        assertEquals(bundle, packageInstance.product());
        assertTrue(packageInstance.serialNumber().isPresent());
        assertEquals(packageSerial, packageInstance.serialNumber().get());
        assertEquals(2, packageInstance.selection().size());
    }

    @Test
    void shouldBuildPackageInstanceWithBatch() {
        ProductInstance laptopInstance = new InstanceBuilder(InstanceId.newOne())
                .withSerial(SerialNumber.of("LAPTOP-001"))
                .asProductInstance(laptop)
                .withQuantity(Quantity.of(1, Unit.pieces()))
                .build();

        ProductInstance mouseInstance = new InstanceBuilder(InstanceId.newOne())
                .withBatch(BatchId.of("MOUSE-BATCH-001"))
                .asProductInstance(mouse)
                .withQuantity(Quantity.of(1, Unit.pieces()))
                .build();

        // For this test, create batch-tracked package
        PackageType batchBundle = ProductBuilder.builder()
                .asPackageType(
                        UuidProductIdentifier.random(),
                        ProductName.of("Batch Bundle"),
                        ProductDescription.of("Batch tracked"),
                        Unit.pieces(),
                        ProductTrackingStrategy.TRACKED_BY_BATCH
                )
                .withSelectionRule(SelectionRule.and(
                        SelectionRule.required(ProductSet.of(laptop.id())),
                        SelectionRule.required(ProductSet.of(mouse.id()))
                ))
                .build();

        BatchId packageBatch = BatchId.of("BUNDLE-BATCH-001");

        PackageInstance packageInstance = new InstanceBuilder(InstanceId.newOne())
                .withBatch(packageBatch)
                .asPackageInstance(batchBundle)
                .withSelection(List.of(
                        new SelectedInstance(laptopInstance, 1),
                        new SelectedInstance(mouseInstance, 1)
                ))
                .build();

        assertNotNull(packageInstance);
        assertFalse(packageInstance.serialNumber().isPresent());
        assertTrue(packageInstance.batchId().isPresent());
        assertEquals(packageBatch, packageInstance.batchId().get());
    }

    @Test
    void shouldSupportFluentBuildingStyle() {
        // Demonstrates the fluent API similar to ProductBuilder
        ProductInstance instance = new InstanceBuilder(InstanceId.newOne())
                .withSerial(SerialNumber.of("LAPTOP-001"))
                .withBatch(BatchId.of("BATCH-2025-Q1"))
                .asProductInstance(laptop)
                .withQuantity(Quantity.of(1, Unit.pieces()))
                .withFeature(colorFeature, "Space Gray")
                .build();

        assertNotNull(instance);
        assertTrue(instance.serialNumber().isPresent());
        assertTrue(instance.batchId().isPresent());
        assertEquals(1, instance.features().size());
    }

    @Test
    void shouldAllowSwitchingBetweenProductAndPackageBuilders() {
        InstanceId id1 = InstanceId.newOne();
        InstanceId id2 = InstanceId.newOne();

        // Build product instance
        InstanceBuilder builder1 = new InstanceBuilder(id1);
        ProductInstance productInstance = builder1
                .withSerial(SerialNumber.of("PROD-001"))
                .asProductInstance(laptop)
                .withQuantity(Quantity.of(1, Unit.pieces()))
                .build();

        // Build package instance
        InstanceBuilder builder2 = new InstanceBuilder(id2);
        PackageInstance packageInstance = builder2
                .withSerial(SerialNumber.of("PKG-001"))
                .asPackageInstance(bundle)
                .withSelection(List.of(
                        new SelectedInstance(productInstance, 1),
                        new SelectedInstance(
                                new InstanceBuilder(InstanceId.newOne())
                                        .withBatch(BatchId.of("MOUSE-BATCH"))
                                        .asProductInstance(mouse)
                                        .withQuantity(Quantity.of(1, Unit.pieces()))
                                        .build(),
                                1
                        )
                ))
                .build();

        assertNotNull(productInstance);
        assertNotNull(packageInstance);
        assertEquals(id1, productInstance.id());
        assertEquals(id2, packageInstance.id());
    }

    @Test
    void shouldPreserveCommonFieldsWhenBuildingProductInstance() {
        SerialNumber serial = SerialNumber.of("COMMON-001");
        BatchId batch = BatchId.of("COMMON-BATCH");

        ProductInstance instance = new InstanceBuilder(InstanceId.newOne())
                .withSerial(serial)
                .withBatch(batch)
                .asProductInstance(laptop)
                .withQuantity(Quantity.of(1, Unit.pieces()))
                .build();

        assertEquals(serial, instance.serialNumber().get());
        assertEquals(batch, instance.batchId().get());
    }

    @Test
    void shouldPreserveCommonFieldsWhenBuildingPackageInstance() {
        SerialNumber serial = SerialNumber.of("COMMON-001");
        BatchId batch = BatchId.of("COMMON-BATCH");

        ProductInstance laptopInstance = new InstanceBuilder(InstanceId.newOne())
                .withSerial(SerialNumber.of("LAPTOP-001"))
                .asProductInstance(laptop)
                .withQuantity(Quantity.of(1, Unit.pieces()))
                .build();

        ProductInstance mouseInstance = new InstanceBuilder(InstanceId.newOne())
                .withBatch(BatchId.of("MOUSE-BATCH"))
                .asProductInstance(mouse)
                .withQuantity(Quantity.of(1, Unit.pieces()))
                .build();

        PackageInstance instance = new InstanceBuilder(InstanceId.newOne())
                .withSerial(serial)
                .withBatch(batch)
                .asPackageInstance(bundle)
                .withSelection(List.of(
                        new SelectedInstance(laptopInstance, 1),
                        new SelectedInstance(mouseInstance, 1)
                ))
                .build();

        assertEquals(serial, instance.serialNumber().get());
        assertEquals(batch, instance.batchId().get());
    }

    @Test
    void shouldDemonstrateParallelStructureWithProductBuilder() {
        // ProductBuilder: defines TYPES
        PackageType packageType = ProductBuilder.builder()
                .asPackageType(
                        UuidProductIdentifier.random(),
                        ProductName.of("Test Package"),
                        ProductDescription.of("Test"),
                        Unit.pieces(),
                        ProductTrackingStrategy.INDIVIDUALLY_TRACKED
                )
                .withSelectionRule(SelectionRule.required(ProductSet.of(laptop.id())))
                .build();

        assertNotNull(packageType);

        // InstanceBuilder: creates INSTANCES
        PackageInstance packageInstance = new InstanceBuilder(InstanceId.newOne())
                .withSerial(SerialNumber.of("INSTANCE-001"))
                .asPackageInstance(packageType)
                .withSelection(List.of(
                        new SelectedInstance(
                                new InstanceBuilder(InstanceId.newOne())
                                        .withSerial(SerialNumber.of("LAPTOP-001"))
                                        .asProductInstance(laptop)
                                        .withQuantity(Quantity.of(1, Unit.pieces()))
                                        .build(),
                                1
                        )
                ))
                .build();

        assertNotNull(packageInstance);
        assertEquals(packageType, packageInstance.packageType());
    }

    @Test
    void shouldCreateInstanceWithGeneratedId() {
        InstanceId generatedId = InstanceId.newOne();

        ProductInstance instance = new InstanceBuilder(generatedId)
                .withSerial(SerialNumber.of("AUTO-001"))
                .asProductInstance(laptop)
                .withQuantity(Quantity.of(1, Unit.pieces()))
                .build();

        assertEquals(generatedId, instance.id());
    }

    @Test
    void shouldCreateInstanceWithExplicitId() {
        String explicitIdValue = "123e4567-e89b-12d3-a456-426614174000";
        InstanceId explicitId = InstanceId.of(explicitIdValue);

        ProductInstance instance = new InstanceBuilder(explicitId)
                .withSerial(SerialNumber.of("EXPLICIT-001"))
                .asProductInstance(laptop)
                .withQuantity(Quantity.of(1, Unit.pieces()))
                .build();

        assertEquals(explicitId, instance.id());
        assertEquals(explicitIdValue, instance.id().value().toString());
    }
}
