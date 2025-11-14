package com.softwarearchetypes.product;

import com.softwarearchetypes.quantity.Quantity;
import com.softwarearchetypes.quantity.Unit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PackageInstance creation and validation.
 */
class PackageInstanceTest {

    private ProductType laptop;
    private ProductType mouse;
    private ProductType keyboard;
    private ProductType simCard;
    private PackageType laptopBundle;
    private PackageType telecomPackage;

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
                ProductTrackingStrategy.IDENTICAL
        ).build();

        keyboard = ProductType.builder(
                UuidProductIdentifier.random(),
                ProductName.of("Mechanical Keyboard"),
                ProductDescription.of("RGB keyboard"),
                Unit.pieces(),
                ProductTrackingStrategy.IDENTICAL
        ).build();

        simCard = ProductType.builder(
                UuidProductIdentifier.random(),
                ProductName.of("5G SIM Card"),
                ProductDescription.of("Prepaid SIM"),
                Unit.pieces(),
                ProductTrackingStrategy.INDIVIDUALLY_TRACKED
        ).build();

        // Laptop bundle: laptop + mouse required
        laptopBundle = ProductBuilder.builder()
                .asPackageType(
                        UuidProductIdentifier.random(),
                        ProductName.of("Laptop Bundle"),
                        ProductDescription.of("Complete workstation"),
                        Unit.pieces(),
                        ProductTrackingStrategy.INDIVIDUALLY_TRACKED
                )
                .withSelectionRule(SelectionRule.and(
                        SelectionRule.required(ProductSet.of(laptop.id())),
                        SelectionRule.required(ProductSet.of(mouse.id()))
                ))
                .build();

        // Telecom package: SIM card + at least one accessory (batch tracked)
        telecomPackage = ProductBuilder.builder()
                .asPackageType(
                        UuidProductIdentifier.random(),
                        ProductName.of("5G Starter Pack"),
                        ProductDescription.of("SIM with accessories"),
                        Unit.pieces(),
                        ProductTrackingStrategy.TRACKED_BY_BATCH
                )
                .withSelectionRule(SelectionRule.and(
                        SelectionRule.required(ProductSet.of(simCard.id())),
                        SelectionRule.isSubsetOf(ProductSet.of(mouse.id(), keyboard.id()), 1, 2)
                ))
                .build();
    }

    @Test
    void shouldCreatePackageInstanceWithValidSelection() {
        // Create product instances
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

        // Create package instance
        PackageInstance packageInstance = new InstanceBuilder(InstanceId.newOne())
                .withSerial(SerialNumber.of("BUNDLE-001"))
                .asPackageInstance(laptopBundle)
                .withSelection(List.of(
                        new SelectedInstance(laptopInstance, 1),
                        new SelectedInstance(mouseInstance, 1)
                ))
                .build();

        assertNotNull(packageInstance);
        assertEquals(laptopBundle, packageInstance.packageType());
        assertEquals(2, packageInstance.selection().size());
        assertTrue(packageInstance.serialNumber().isPresent());
        assertEquals("BUNDLE-001", packageInstance.serialNumber().get().value());
    }

    @Test
    void shouldRejectPackageInstanceWithInvalidSelection() {
        // Create only laptop instance (missing required mouse)
        ProductInstance laptopInstance = new InstanceBuilder(InstanceId.newOne())
                .withSerial(SerialNumber.of("LAPTOP-001"))
                .asProductInstance(laptop)
                .withQuantity(Quantity.of(1, Unit.pieces()))
                .build();

        // Should fail: missing required mouse
        assertThrows(IllegalArgumentException.class, () -> {
            new InstanceBuilder(InstanceId.newOne())
                    .withSerial(SerialNumber.of("BUNDLE-001"))
                    .asPackageInstance(laptopBundle)
                    .withSelection(List.of(
                            new SelectedInstance(laptopInstance, 1)
                    ))
                    .build();
        });
    }

    @Test
    void shouldRejectPackageInstanceWithEmptySelection() {
        assertThrows(IllegalArgumentException.class, () -> {
            new InstanceBuilder(InstanceId.newOne())
                    .withSerial(SerialNumber.of("BUNDLE-001"))
                    .asPackageInstance(laptopBundle)
                    .withSelection(List.of())
                    .build();
        });
    }

    @Test
    void shouldEnforceTrackingStrategyForIndividuallyTrackedPackage() {
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

        // Should fail: individually tracked package requires serial number
        assertThrows(IllegalArgumentException.class, () -> {
            new InstanceBuilder(InstanceId.newOne())
                    .withBatch(BatchId.of("BUNDLE-BATCH-001")) // Only batch, no serial
                    .asPackageInstance(laptopBundle)
                    .withSelection(List.of(
                            new SelectedInstance(laptopInstance, 1),
                            new SelectedInstance(mouseInstance, 1)
                    ))
                    .build();
        });
    }

    @Test
    void shouldEnforceTrackingStrategyForBatchTrackedPackage() {
        ProductInstance simInstance = new InstanceBuilder(InstanceId.newOne())
                .withSerial(SerialNumber.of("SIM-IMSI-123456"))
                .asProductInstance(simCard)
                .withQuantity(Quantity.of(1, Unit.pieces()))
                .build();

        ProductInstance mouseInstance = new InstanceBuilder(InstanceId.newOne())
                .withBatch(BatchId.of("MOUSE-BATCH-001"))
                .asProductInstance(mouse)
                .withQuantity(Quantity.of(1, Unit.pieces()))
                .build();

        // Should fail: batch tracked package requires batch ID
        assertThrows(IllegalArgumentException.class, () -> {
            new InstanceBuilder(InstanceId.newOne())
                    .withSerial(SerialNumber.of("PACK-001")) // Only serial, no batch
                    .asPackageInstance(telecomPackage)
                    .withSelection(List.of(
                            new SelectedInstance(simInstance, 1),
                            new SelectedInstance(mouseInstance, 1)
                    ))
                    .build();
        });
    }

    @Test
    void shouldAllowBothSerialAndBatchForPackageInstance() {
        ProductInstance simInstance = new InstanceBuilder(InstanceId.newOne())
                .withSerial(SerialNumber.of("SIM-IMSI-123456"))
                .asProductInstance(simCard)
                .withQuantity(Quantity.of(1, Unit.pieces()))
                .build();

        ProductInstance mouseInstance = new InstanceBuilder(InstanceId.newOne())
                .withBatch(BatchId.of("MOUSE-BATCH-001"))
                .asProductInstance(mouse)
                .withQuantity(Quantity.of(1, Unit.pieces()))
                .build();

        // Valid: has both serial and batch
        PackageInstance packageInstance = new InstanceBuilder(InstanceId.newOne())
                .withSerial(SerialNumber.of("PACK-001"))
                .withBatch(BatchId.of("PACK-BATCH-2025"))
                .asPackageInstance(telecomPackage)
                .withSelection(List.of(
                        new SelectedInstance(simInstance, 1),
                        new SelectedInstance(mouseInstance, 1)
                ))
                .build();

        assertNotNull(packageInstance);
        assertTrue(packageInstance.serialNumber().isPresent());
        assertTrue(packageInstance.batchId().isPresent());
    }

    @Test
    void shouldRejectPackageInstanceWithoutAnyTracking() {
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

        // Should fail: no serial and no batch
        assertThrows(IllegalArgumentException.class, () -> {
            new InstanceBuilder(InstanceId.newOne())
                    // No serial, no batch
                    .asPackageInstance(laptopBundle)
                    .withSelection(List.of(
                            new SelectedInstance(laptopInstance, 1),
                            new SelectedInstance(mouseInstance, 1)
                    ))
                    .build();
        });
    }

    @Test
    void shouldSupportMultipleQuantitiesOfSameInstance() {
        // Create mouse instance representing 1 piece
        ProductInstance mouseInstance = new InstanceBuilder(InstanceId.newOne())
                .withBatch(BatchId.of("MOUSE-BATCH-001"))
                .asProductInstance(mouse)
                .withQuantity(Quantity.of(1, Unit.pieces()))
                .build();

        ProductInstance laptopInstance = new InstanceBuilder(InstanceId.newOne())
                .withSerial(SerialNumber.of("LAPTOP-001"))
                .asProductInstance(laptop)
                .withQuantity(Quantity.of(1, Unit.pieces()))
                .build();

        // Package contains laptop + 2 mice from same batch
        PackageInstance packageInstance = new InstanceBuilder(InstanceId.newOne())
                .withSerial(SerialNumber.of("BUNDLE-001"))
                .asPackageInstance(laptopBundle)
                .withSelection(List.of(
                        new SelectedInstance(laptopInstance, 1),
                        new SelectedInstance(mouseInstance, 2) // 2 mice
                ))
                .build();

        assertNotNull(packageInstance);
        assertEquals(2, packageInstance.selection().size());

        // Find mouse selection
        var mouseSelection = packageInstance.selection().stream()
                .filter(s -> s.product().id().equals(mouse.id()))
                .findFirst()
                .orElseThrow();

        assertEquals(2, mouseSelection.quantity());
    }

    @Test
    void shouldCreateNestedPackageInstance() {
        // Inner package type
        PackageType innerBundle = ProductBuilder.builder()
                .asPackageType(
                        UuidProductIdentifier.random(),
                        ProductName.of("Peripherals Bundle"),
                        ProductDescription.of("Mouse and keyboard"),
                        Unit.pieces(),
                        ProductTrackingStrategy.TRACKED_BY_BATCH
                )
                .withSelectionRule(SelectionRule.and(
                        SelectionRule.required(ProductSet.of(mouse.id())),
                        SelectionRule.required(ProductSet.of(keyboard.id()))
                ))
                .build();

        // Create inner package instance
        ProductInstance mouseInstance = new InstanceBuilder(InstanceId.newOne())
                .withBatch(BatchId.of("MOUSE-BATCH-001"))
                .asProductInstance(mouse)
                .withQuantity(Quantity.of(1, Unit.pieces()))
                .build();

        ProductInstance keyboardInstance = new InstanceBuilder(InstanceId.newOne())
                .withBatch(BatchId.of("KEYBOARD-BATCH-001"))
                .asProductInstance(keyboard)
                .withQuantity(Quantity.of(1, Unit.pieces()))
                .build();

        PackageInstance innerPackageInstance = new InstanceBuilder(InstanceId.newOne())
                .withBatch(BatchId.of("PERIPHERALS-BATCH-001"))
                .asPackageInstance(innerBundle)
                .withSelection(List.of(
                        new SelectedInstance(mouseInstance, 1),
                        new SelectedInstance(keyboardInstance, 1)
                ))
                .build();

        // Outer package type
        PackageType outerBundle = ProductBuilder.builder()
                .asPackageType(
                        UuidProductIdentifier.random(),
                        ProductName.of("Complete Workstation"),
                        ProductDescription.of("Laptop with peripherals bundle"),
                        Unit.pieces(),
                        ProductTrackingStrategy.INDIVIDUALLY_TRACKED
                )
                .withSelectionRule(SelectionRule.and(
                        SelectionRule.required(ProductSet.of(laptop.id())),
                        SelectionRule.required(ProductSet.of(innerBundle.id()))
                ))
                .build();

        // Create outer package instance
        ProductInstance laptopInstance = new InstanceBuilder(InstanceId.newOne())
                .withSerial(SerialNumber.of("LAPTOP-001"))
                .asProductInstance(laptop)
                .withQuantity(Quantity.of(1, Unit.pieces()))
                .build();

        PackageInstance outerPackageInstance = new InstanceBuilder(InstanceId.newOne())
                .withSerial(SerialNumber.of("WORKSTATION-001"))
                .asPackageInstance(outerBundle)
                .withSelection(List.of(
                        new SelectedInstance(laptopInstance, 1),
                        new SelectedInstance(innerPackageInstance, 1)
                ))
                .build();

        assertNotNull(outerPackageInstance);
        assertEquals(2, outerPackageInstance.selection().size());

        // Verify nested structure
        var nestedPackageSelection = outerPackageInstance.selection().stream()
                .filter(s -> s.instance() instanceof PackageInstance)
                .findFirst()
                .orElseThrow();

        PackageInstance nestedPackage = (PackageInstance) nestedPackageSelection.instance();
        assertEquals(innerBundle, nestedPackage.packageType());
        assertEquals(2, nestedPackage.selection().size());
    }

    @Test
    void shouldProvideAccessToPackageInstanceProperties() {
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
                .asPackageInstance(laptopBundle)
                .withSelection(List.of(
                        new SelectedInstance(laptopInstance, 1),
                        new SelectedInstance(mouseInstance, 1)
                ))
                .build();

        assertEquals(packageId, packageInstance.id());
        assertEquals(laptopBundle, packageInstance.product());
        assertEquals(laptopBundle, packageInstance.packageType());
        assertTrue(packageInstance.serialNumber().isPresent());
        assertEquals(packageSerial, packageInstance.serialNumber().get());
        assertEquals(2, packageInstance.selection().size());
    }

    @Test
    void shouldGenerateReadableToString() {
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

        PackageInstance packageInstance = new InstanceBuilder(InstanceId.newOne())
                .withSerial(SerialNumber.of("BUNDLE-001"))
                .asPackageInstance(laptopBundle)
                .withSelection(List.of(
                        new SelectedInstance(laptopInstance, 1),
                        new SelectedInstance(mouseInstance, 1)
                ))
                .build();

        String toString = packageInstance.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("PackageInstance"));
        assertTrue(toString.contains("Laptop Bundle"));
        assertTrue(toString.contains("BUNDLE-001"));
        assertTrue(toString.contains("2 products"));
    }
}
