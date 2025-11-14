package com.softwarearchetypes.product;

import com.softwarearchetypes.quantity.Quantity;
import com.softwarearchetypes.quantity.Unit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PackageType creation and selection validation.
 */
class PackageTypeTest {

    private ProductType laptop;
    private ProductType mouse;
    private ProductType keyboard;
    private ProductType monitor;
    private ProductType warranty;
    private ProductType insurance;

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

        monitor = ProductType.builder(
                UuidProductIdentifier.random(),
                ProductName.of("4K Monitor"),
                ProductDescription.of("27-inch display"),
                Unit.pieces(),
                ProductTrackingStrategy.INDIVIDUALLY_TRACKED
        ).build();

        warranty = ProductType.builder(
                UuidProductIdentifier.random(),
                ProductName.of("Extended Warranty"),
                ProductDescription.of("3-year warranty"),
                Unit.pieces(),
                ProductTrackingStrategy.IDENTICAL
        ).build();

        insurance = ProductType.builder(
                UuidProductIdentifier.random(),
                ProductName.of("Device Insurance"),
                ProductDescription.of("Accidental damage coverage"),
                Unit.pieces(),
                ProductTrackingStrategy.IDENTICAL
        ).build();
    }

    @Test
    void shouldCreateSimplePackageWithRequiredProduct() {
        ProductSet laptopSet = ProductSet.of(laptop.id());
        SelectionRule rule = SelectionRule.required(laptopSet);

        PackageType bundle = ProductBuilder.builder()
                .asPackageType(
                        UuidProductIdentifier.random(),
                        ProductName.of("Laptop Bundle"),
                        ProductDescription.of("Basic laptop package"),
                        Unit.pieces(),
                        ProductTrackingStrategy.INDIVIDUALLY_TRACKED
                )
                .withSelectionRule(rule)
                .build();

        assertNotNull(bundle);
        assertEquals("Laptop Bundle", bundle.name());
        assertEquals(1, bundle.structure().selectionRules().size());
    }

    @Test
    void shouldValidateSelectionWithRequiredRule() {
        ProductSet laptopSet = ProductSet.of(laptop.id());
        SelectionRule rule = SelectionRule.required(laptopSet);

        PackageType bundle = ProductBuilder.builder()
                .asPackageType(
                        UuidProductIdentifier.random(),
                        ProductName.of("Laptop Bundle"),
                        ProductDescription.of("Basic laptop package"),
                        Unit.pieces(),
                        ProductTrackingStrategy.INDIVIDUALLY_TRACKED
                )
                .withSelectionRule(rule)
                .build();

        // Valid: contains required laptop
        var validSelection = List.of(new SelectedProduct(laptop.id(), 1));
        var result = bundle.validateSelection(validSelection);
        assertTrue(result.isValid(), "Selection with required laptop should be valid");
        assertTrue(result.errors().isEmpty());

        // Invalid: missing required laptop
        var invalidSelection = List.<SelectedProduct>of();
        var invalidResult = bundle.validateSelection(invalidSelection);
        assertFalse(invalidResult.isValid(), "Empty selection should be invalid");
        assertFalse(invalidResult.errors().isEmpty());
    }

    @Test
    void shouldValidateSelectionWithOptionalRule() {
        ProductSet warrantySet = ProductSet.of(warranty.id());
        SelectionRule rule = SelectionRule.optional(warrantySet);

        PackageType bundle = ProductBuilder.builder()
                .asPackageType(
                        UuidProductIdentifier.random(),
                        ProductName.of("Laptop with Optional Warranty"),
                        ProductDescription.of("Laptop package"),
                        Unit.pieces(),
                        ProductTrackingStrategy.INDIVIDUALLY_TRACKED
                )
                .withSelectionRule(rule)
                .build();

        // Valid: no warranty
        var withoutWarranty = List.<SelectedProduct>of();
        assertTrue(bundle.validateSelection(withoutWarranty).isValid());

        // Valid: with warranty
        var withWarranty = List.of(new SelectedProduct(warranty.id(), 1));
        assertTrue(bundle.validateSelection(withWarranty).isValid());
    }

    @Test
    void shouldValidateSelectionWithAndRule() {
        ProductSet laptopSet = ProductSet.of(laptop.id());
        ProductSet mouseSet = ProductSet.of(mouse.id());

        SelectionRule rule = SelectionRule.and(
                SelectionRule.required(laptopSet),
                SelectionRule.required(mouseSet)
        );

        PackageType bundle = ProductBuilder.builder()
                .asPackageType(
                        UuidProductIdentifier.random(),
                        ProductName.of("Laptop + Mouse Bundle"),
                        ProductDescription.of("Complete workstation"),
                        Unit.pieces(),
                        ProductTrackingStrategy.INDIVIDUALLY_TRACKED
                )
                .withSelectionRule(rule)
                .build();

        // Valid: both laptop and mouse
        var validSelection = List.of(
                new SelectedProduct(laptop.id(), 1),
                new SelectedProduct(mouse.id(), 1)
        );
        assertTrue(bundle.validateSelection(validSelection).isValid());

        // Invalid: only laptop
        var onlyLaptop = List.of(new SelectedProduct(laptop.id(), 1));
        assertFalse(bundle.validateSelection(onlyLaptop).isValid());

        // Invalid: only mouse
        var onlyMouse = List.of(new SelectedProduct(mouse.id(), 1));
        assertFalse(bundle.validateSelection(onlyMouse).isValid());
    }

    @Test
    void shouldValidateSelectionWithOrRule() {
        ProductSet mouseSet = ProductSet.of(mouse.id());
        ProductSet keyboardSet = ProductSet.of(keyboard.id());

        SelectionRule rule = SelectionRule.or(
                SelectionRule.required(mouseSet),
                SelectionRule.required(keyboardSet)
        );

        PackageType bundle = ProductBuilder.builder()
                .asPackageType(
                        UuidProductIdentifier.random(),
                        ProductName.of("Input Device Bundle"),
                        ProductDescription.of("Choose mouse or keyboard"),
                        Unit.pieces(),
                        ProductTrackingStrategy.IDENTICAL
                )
                .withSelectionRule(rule)
                .build();

        // Valid: mouse
        var withMouse = List.of(new SelectedProduct(mouse.id(), 1));
        assertTrue(bundle.validateSelection(withMouse).isValid());

        // Valid: keyboard
        var withKeyboard = List.of(new SelectedProduct(keyboard.id(), 1));
        assertTrue(bundle.validateSelection(withKeyboard).isValid());

        // Valid: both
        var withBoth = List.of(
                new SelectedProduct(mouse.id(), 1),
                new SelectedProduct(keyboard.id(), 1)
        );
        assertTrue(bundle.validateSelection(withBoth).isValid());

        // Invalid: neither
        var withNeither = List.<SelectedProduct>of();
        assertFalse(bundle.validateSelection(withNeither).isValid());
    }

    @Test
    void shouldValidateSelectionWithNotRule() {
        ProductSet insuranceSet = ProductSet.of(insurance.id());

        SelectionRule rule = SelectionRule.not(
                SelectionRule.required(insuranceSet)
        );

        PackageType bundle = ProductBuilder.builder()
                .asPackageType(
                        UuidProductIdentifier.random(),
                        ProductName.of("No Insurance Bundle"),
                        ProductDescription.of("Insurance not allowed"),
                        Unit.pieces(),
                        ProductTrackingStrategy.IDENTICAL
                )
                .withSelectionRule(rule)
                .build();

        // Valid: no insurance
        var withoutInsurance = List.<SelectedProduct>of();
        assertTrue(bundle.validateSelection(withoutInsurance).isValid());

        // Invalid: with insurance
        var withInsurance = List.of(new SelectedProduct(insurance.id(), 1));
        assertFalse(bundle.validateSelection(withInsurance).isValid());
    }

    @Test
    void shouldValidateSelectionWithConditionalRule() {
        ProductSet laptopSet = ProductSet.of(laptop.id());
        ProductSet warrantySet = ProductSet.of(warranty.id());

        // IF laptop THEN warranty required
        SelectionRule rule = SelectionRule.ifThen(
                SelectionRule.required(laptopSet),
                SelectionRule.required(warrantySet)
        );

        PackageType bundle = ProductBuilder.builder()
                .asPackageType(
                        UuidProductIdentifier.random(),
                        ProductName.of("Laptop with Mandatory Warranty"),
                        ProductDescription.of("Warranty required for laptop"),
                        Unit.pieces(),
                        ProductTrackingStrategy.INDIVIDUALLY_TRACKED
                )
                .withSelectionRule(rule)
                .build();

        // Valid: no laptop, no warranty (condition not met)
        var noLaptop = List.<SelectedProduct>of();
        assertTrue(bundle.validateSelection(noLaptop).isValid());

        // Valid: laptop + warranty
        var laptopWithWarranty = List.of(
                new SelectedProduct(laptop.id(), 1),
                new SelectedProduct(warranty.id(), 1)
        );
        assertTrue(bundle.validateSelection(laptopWithWarranty).isValid());

        // Invalid: laptop without warranty
        var laptopWithoutWarranty = List.of(new SelectedProduct(laptop.id(), 1));
        assertFalse(bundle.validateSelection(laptopWithoutWarranty).isValid());
    }

    @Test
    void shouldValidateComplexSelectionWithMultipleRules() {
        ProductSet laptopSet = ProductSet.of(laptop.id());
        ProductSet accessoriesSet = ProductSet.of(mouse.id(), keyboard.id());
        ProductSet warrantySet = ProductSet.of(warranty.id());

        // Laptop required + at least one accessory + optional warranty
        SelectionRule rule = SelectionRule.and(
                SelectionRule.required(laptopSet),
                SelectionRule.isSubsetOf(accessoriesSet, 1, 2),
                SelectionRule.optional(warrantySet)
        );

        PackageType bundle = ProductBuilder.builder()
                .asPackageType(
                        UuidProductIdentifier.random(),
                        ProductName.of("Complete Workstation"),
                        ProductDescription.of("Laptop with accessories"),
                        Unit.pieces(),
                        ProductTrackingStrategy.INDIVIDUALLY_TRACKED
                )
                .withSelectionRule(rule)
                .build();

        // Valid: laptop + mouse
        var laptopAndMouse = List.of(
                new SelectedProduct(laptop.id(), 1),
                new SelectedProduct(mouse.id(), 1)
        );
        assertTrue(bundle.validateSelection(laptopAndMouse).isValid());

        // Valid: laptop + mouse + keyboard + warranty
        var fullBundle = List.of(
                new SelectedProduct(laptop.id(), 1),
                new SelectedProduct(mouse.id(), 1),
                new SelectedProduct(keyboard.id(), 1),
                new SelectedProduct(warranty.id(), 1)
        );
        assertTrue(bundle.validateSelection(fullBundle).isValid());

        // Invalid: laptop only (missing accessories)
        var laptopOnly = List.of(new SelectedProduct(laptop.id(), 1));
        assertFalse(bundle.validateSelection(laptopOnly).isValid());

        // Invalid: no laptop
        var noLaptop = List.of(new SelectedProduct(mouse.id(), 1));
        assertFalse(bundle.validateSelection(noLaptop).isValid());
    }

    @Test
    void shouldValidateIsSubsetOfWithQuantityConstraints() {
        ProductSet accessoriesSet = ProductSet.of(mouse.id(), keyboard.id(), monitor.id());

        // Select 2 to 3 accessories
        SelectionRule rule = SelectionRule.isSubsetOf(accessoriesSet, 2, 3);

        PackageType bundle = ProductBuilder.builder()
                .asPackageType(
                        UuidProductIdentifier.random(),
                        ProductName.of("Accessories Bundle"),
                        ProductDescription.of("Choose 2-3 accessories"),
                        Unit.pieces(),
                        ProductTrackingStrategy.IDENTICAL
                )
                .withSelectionRule(rule)
                .build();

        // Invalid: too few (1)
        var tooFew = List.of(new SelectedProduct(mouse.id(), 1));
        assertFalse(bundle.validateSelection(tooFew).isValid());

        // Valid: exactly 2
        var exactly2 = List.of(
                new SelectedProduct(mouse.id(), 1),
                new SelectedProduct(keyboard.id(), 1)
        );
        assertTrue(bundle.validateSelection(exactly2).isValid());

        // Valid: exactly 3
        var exactly3 = List.of(
                new SelectedProduct(mouse.id(), 1),
                new SelectedProduct(keyboard.id(), 1),
                new SelectedProduct(monitor.id(), 1)
        );
        assertTrue(bundle.validateSelection(exactly3).isValid());

        // Invalid: too many (4 - but we only have 3 products, so test with quantities)
        var tooMany = List.of(
                new SelectedProduct(mouse.id(), 2),
                new SelectedProduct(keyboard.id(), 1),
                new SelectedProduct(monitor.id(), 1)
        );
        assertFalse(bundle.validateSelection(tooMany).isValid());
    }

    @Test
    void shouldCreateNestedPackage() {
        // Inner package: laptop + mouse
        ProductSet innerSet = ProductSet.of(laptop.id(), mouse.id());
        PackageType innerBundle = ProductBuilder.builder()
                .asPackageType(
                        UuidProductIdentifier.random(),
                        ProductName.of("Basic Bundle"),
                        ProductDescription.of("Laptop and mouse"),
                        Unit.pieces(),
                        ProductTrackingStrategy.INDIVIDUALLY_TRACKED
                )
                .withSelectionRule(SelectionRule.and(
                        SelectionRule.required(ProductSet.of(laptop.id())),
                        SelectionRule.required(ProductSet.of(mouse.id()))
                ))
                .build();

        // Outer package: basic bundle + optional monitor
        ProductSet outerSet = ProductSet.of(innerBundle.id(), monitor.id());
        PackageType outerBundle = ProductBuilder.builder()
                .asPackageType(
                        UuidProductIdentifier.random(),
                        ProductName.of("Premium Bundle"),
                        ProductDescription.of("Basic bundle with optional monitor"),
                        Unit.pieces(),
                        ProductTrackingStrategy.INDIVIDUALLY_TRACKED
                )
                .withSelectionRule(SelectionRule.and(
                        SelectionRule.required(ProductSet.of(innerBundle.id())),
                        SelectionRule.optional(ProductSet.of(monitor.id()))
                ))
                .build();

        assertNotNull(outerBundle);
        assertEquals("Premium Bundle", outerBundle.name());
        assertEquals(1, outerBundle.structure().selectionRules().size());
    }

    @Test
    void shouldRejectInvalidPackageTypeCreation() {
        assertThrows(IllegalArgumentException.class, () -> {
            ProductBuilder.builder()
                    .asPackageType(
                            null, // null ID
                            ProductName.of("Invalid Package"),
                            ProductDescription.of("Missing ID"),
                            Unit.pieces(),
                            ProductTrackingStrategy.IDENTICAL
                    )
                    .build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            ProductBuilder.builder()
                    .asPackageType(
                            UuidProductIdentifier.random(),
                            null, // null name
                            ProductDescription.of("Missing name"),
                            Unit.pieces(),
                            ProductTrackingStrategy.IDENTICAL
                    )
                    .build();
        });
    }

    @Test
    void shouldProvideAccessToPackageStructure() {
        ProductSet laptopSet = ProductSet.of(laptop.id());
        SelectionRule rule1 = SelectionRule.required(laptopSet);
        SelectionRule rule2 = SelectionRule.optional(ProductSet.of(warranty.id()));

        PackageType bundle = ProductBuilder.builder()
                .asPackageType(
                        UuidProductIdentifier.random(),
                        ProductName.of("Laptop Bundle"),
                        ProductDescription.of("Laptop with optional warranty"),
                        Unit.pieces(),
                        ProductTrackingStrategy.INDIVIDUALLY_TRACKED
                )
                .withSelectionRule(rule1)
                .withSelectionRule(rule2)
                .build();

        PackageStructure structure = bundle.structure();
        assertNotNull(structure);
        assertEquals(2, structure.selectionRules().size());
    }
}
