package com.softwarearchetypes.product;

import com.softwarearchetypes.quantity.Quantity;
import com.softwarearchetypes.quantity.Unit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SelectedInstance record.
 */
class SelectedInstanceTest {

    private ProductType laptop;
    private ProductType mouse;
    private PackageType bundle;
    private ProductInstance laptopInstance;
    private ProductInstance mouseInstance;
    private PackageInstance bundleInstance;

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
                .withSelectionRule(SelectionRule.required(ProductSet.of(laptop.id())))
                .build();

        laptopInstance = new InstanceBuilder(InstanceId.newOne())
                .withSerial(SerialNumber.of("LAPTOP-001"))
                .asProductInstance(laptop)
                .withQuantity(Quantity.of(1, Unit.pieces()))
                .build();

        mouseInstance = new InstanceBuilder(InstanceId.newOne())
                .withBatch(BatchId.of("MOUSE-BATCH-001"))
                .asProductInstance(mouse)
                .withQuantity(Quantity.of(1, Unit.pieces()))
                .build();

        bundleInstance = new InstanceBuilder(InstanceId.newOne())
                .withSerial(SerialNumber.of("BUNDLE-001"))
                .asPackageInstance(bundle)
                .withSelection(java.util.List.of(
                        new SelectedInstance(laptopInstance, 1)
                ))
                .build();
    }

    @Test
    void shouldCreateSelectedInstanceWithProductInstance() {
        SelectedInstance selected = new SelectedInstance(laptopInstance, 1);

        assertNotNull(selected);
        assertEquals(laptopInstance, selected.instance());
        assertEquals(1, selected.quantity());
    }

    @Test
    void shouldCreateSelectedInstanceWithPackageInstance() {
        SelectedInstance selected = new SelectedInstance(bundleInstance, 1);

        assertNotNull(selected);
        assertEquals(bundleInstance, selected.instance());
        assertEquals(1, selected.quantity());
    }

    @Test
    void shouldCreateSelectedInstanceWithMultipleQuantity() {
        SelectedInstance selected = new SelectedInstance(mouseInstance, 5);

        assertEquals(mouseInstance, selected.instance());
        assertEquals(5, selected.quantity());
    }

    @Test
    void shouldRejectNullInstance() {
        assertThrows(IllegalArgumentException.class, () -> {
            new SelectedInstance(null, 1);
        });
    }

    @Test
    void shouldRejectZeroQuantity() {
        assertThrows(IllegalArgumentException.class, () -> {
            new SelectedInstance(laptopInstance, 0);
        });
    }

    @Test
    void shouldRejectNegativeQuantity() {
        assertThrows(IllegalArgumentException.class, () -> {
            new SelectedInstance(laptopInstance, -1);
        });
    }

    @Test
    void shouldProvideAccessToProduct() {
        SelectedInstance selected = new SelectedInstance(laptopInstance, 1);

        Product product = selected.product();
        assertNotNull(product);
        assertEquals(laptop, product);
    }

    @Test
    void shouldProvideAccessToProductId() {
        SelectedInstance selected = new SelectedInstance(laptopInstance, 1);

        ProductIdentifier productId = selected.productId();
        assertNotNull(productId);
        assertEquals(laptop.id(), productId);
    }

    @Test
    void shouldProvideAccessToInstanceId() {
        SelectedInstance selected = new SelectedInstance(laptopInstance, 1);

        InstanceId instanceId = selected.instanceId();
        assertNotNull(instanceId);
        assertEquals(laptopInstance.id(), instanceId);
    }

    @Test
    void shouldConvertToSelectedProduct() {
        SelectedInstance selected = new SelectedInstance(laptopInstance, 2);

        SelectedProduct selectedProduct = selected.toSelectedProduct();

        assertNotNull(selectedProduct);
        assertEquals(laptop.id(), selectedProduct.productIdentifier());
        assertEquals(2, selectedProduct.quantity());
    }

    @Test
    void shouldConvertToSelectedProductForPackageInstance() {
        SelectedInstance selected = new SelectedInstance(bundleInstance, 3);

        SelectedProduct selectedProduct = selected.toSelectedProduct();

        assertNotNull(selectedProduct);
        assertEquals(bundle.id(), selectedProduct.productIdentifier());
        assertEquals(3, selectedProduct.quantity());
    }

    @Test
    void shouldMaintainQuantityWhenConvertingToSelectedProduct() {
        int quantity = 10;
        SelectedInstance selected = new SelectedInstance(mouseInstance, quantity);

        SelectedProduct selectedProduct = selected.toSelectedProduct();

        assertEquals(quantity, selectedProduct.quantity());
    }

    @Test
    void shouldSupportEqualityBasedOnInstanceAndQuantity() {
        SelectedInstance selected1 = new SelectedInstance(laptopInstance, 1);
        SelectedInstance selected2 = new SelectedInstance(laptopInstance, 1);
        SelectedInstance selected3 = new SelectedInstance(laptopInstance, 2);
        SelectedInstance selected4 = new SelectedInstance(mouseInstance, 1);

        // Same instance and quantity
        assertEquals(selected1, selected2);
        assertEquals(selected1.hashCode(), selected2.hashCode());

        // Different quantity
        assertNotEquals(selected1, selected3);

        // Different instance
        assertNotEquals(selected1, selected4);
    }

    @Test
    void shouldWorkAsRecordWithToString() {
        SelectedInstance selected = new SelectedInstance(laptopInstance, 2);

        String toString = selected.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("SelectedInstance"));
        assertTrue(toString.contains("2"));
    }

    @Test
    void shouldAllowAccessToUnderlyingInstanceProperties() {
        SelectedInstance selected = new SelectedInstance(laptopInstance, 1);

        // Through instance()
        Instance instance = selected.instance();
        assertTrue(instance.serialNumber().isPresent());
        assertEquals("LAPTOP-001", instance.serialNumber().get().value());

        // Through product()
        Product product = selected.product();
        assertEquals("Business Laptop", product.name());
    }

    @Test
    void shouldWorkWithProductInstanceHavingBatch() {
        SelectedInstance selected = new SelectedInstance(mouseInstance, 3);

        Instance instance = selected.instance();
        assertTrue(instance.batchId().isPresent());
        assertEquals("MOUSE-BATCH-001", instance.batchId().get().value());
    }

    @Test
    void shouldDistinguishBetweenDifferentInstancesOfSameProduct() {
        // Two different instances of same product type
        ProductInstance laptop1 = new InstanceBuilder(InstanceId.newOne())
                .withSerial(SerialNumber.of("LAPTOP-001"))
                .asProductInstance(laptop)
                .withQuantity(Quantity.of(1, Unit.pieces()))
                .build();

        ProductInstance laptop2 = new InstanceBuilder(InstanceId.newOne())
                .withSerial(SerialNumber.of("LAPTOP-002"))
                .asProductInstance(laptop)
                .withQuantity(Quantity.of(1, Unit.pieces()))
                .build();

        SelectedInstance selected1 = new SelectedInstance(laptop1, 1);
        SelectedInstance selected2 = new SelectedInstance(laptop2, 1);

        // Same product type but different instances
        assertEquals(selected1.productId(), selected2.productId());
        assertNotEquals(selected1.instanceId(), selected2.instanceId());
        assertNotEquals(selected1, selected2);
    }

    @Test
    void shouldSupportNestedAccess() {
        // Package contains laptop
        SelectedInstance selectedBundle = new SelectedInstance(bundleInstance, 1);

        // Access nested structure
        PackageInstance pkg = (PackageInstance) selectedBundle.instance();
        assertEquals(1, pkg.selection().size());

        SelectedInstance nestedLaptop = pkg.selection().get(0);
        assertEquals(laptopInstance.id(), nestedLaptop.instanceId());
    }
}
