package com.softwarearchetypes.product;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.softwarearchetypes.product.ApplicabilityConstraint.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicabilityConstraintTest {

    @Test
    void shouldSatisfyEqualsConstraint() {
        var constraint = equals("country", "PL");
        var context = ApplicabilityContext.of(Map.of("country", "PL"));

        assertTrue(constraint.isSatisfiedBy(context));
    }

    @Test
    void shouldNotSatisfyEqualsConstraintWhenValueDifferent() {
        var constraint = equals("country", "PL");
        var context = ApplicabilityContext.of(Map.of("country", "UK"));

        assertFalse(constraint.isSatisfiedBy(context));
    }

    @Test
    void shouldNotSatisfyEqualsConstraintWhenParameterMissing() {
        var constraint = equals("country", "PL");
        var context = ApplicabilityContext.empty();

        assertFalse(constraint.isSatisfiedBy(context));
    }

    @Test
    void shouldSatisfyInConstraintWhenValueInSet() {
        var constraint = in("channel", "mobile", "web", "tablet");
        var context = ApplicabilityContext.of(Map.of("channel", "mobile"));

        assertTrue(constraint.isSatisfiedBy(context));
    }

    @Test
    void shouldNotSatisfyInConstraintWhenValueNotInSet() {
        var constraint = in("channel", "mobile", "web");
        var context = ApplicabilityContext.of(Map.of("channel", "desktop"));

        assertFalse(constraint.isSatisfiedBy(context));
    }

    @Test
    void shouldSatisfyGreaterThanConstraint() {
        var constraint = greaterThan("age", 18);
        var context = ApplicabilityContext.of(Map.of("age", "25"));

        assertTrue(constraint.isSatisfiedBy(context));
    }

    @Test
    void shouldNotSatisfyGreaterThanConstraintWhenEqual() {
        var constraint = greaterThan("age", 18);
        var context = ApplicabilityContext.of(Map.of("age", "18"));

        assertFalse(constraint.isSatisfiedBy(context));
    }

    @Test
    void shouldNotSatisfyGreaterThanConstraintWhenLess() {
        var constraint = greaterThan("age", 18);
        var context = ApplicabilityContext.of(Map.of("age", "15"));

        assertFalse(constraint.isSatisfiedBy(context));
    }

    @Test
    void shouldNotSatisfyGreaterThanConstraintWhenNotNumeric() {
        var constraint = greaterThan("age", 18);
        var context = ApplicabilityContext.of(Map.of("age", "adult"));

        assertFalse(constraint.isSatisfiedBy(context));
    }

    @Test
    void shouldSatisfyLessThanConstraint() {
        var constraint = lessThan("age", 16);
        var context = ApplicabilityContext.of(Map.of("age", "12"));

        assertTrue(constraint.isSatisfiedBy(context));
    }

    @Test
    void shouldNotSatisfyLessThanConstraintWhenGreater() {
        var constraint = lessThan("age", 16);
        var context = ApplicabilityContext.of(Map.of("age", "20"));

        assertFalse(constraint.isSatisfiedBy(context));
    }

    @Test
    void shouldSatisfyBetweenConstraint() {
        var constraint = between("age", 18, 65);
        var context = ApplicabilityContext.of(Map.of("age", "30"));

        assertTrue(constraint.isSatisfiedBy(context));
    }

    @Test
    void shouldSatisfyBetweenConstraintAtMinBoundary() {
        var constraint = between("age", 18, 65);
        var context = ApplicabilityContext.of(Map.of("age", "18"));

        assertTrue(constraint.isSatisfiedBy(context));
    }

    @Test
    void shouldSatisfyBetweenConstraintAtMaxBoundary() {
        var constraint = between("age", 18, 65);
        var context = ApplicabilityContext.of(Map.of("age", "65"));

        assertTrue(constraint.isSatisfiedBy(context));
    }

    @Test
    void shouldNotSatisfyBetweenConstraintWhenOutsideRange() {
        var constraint = between("age", 18, 65);
        var context = ApplicabilityContext.of(Map.of("age", "70"));

        assertFalse(constraint.isSatisfiedBy(context));
    }

    @Test
    void shouldSatisfyAndConstraintWhenAllConstraintsMet() {
        var constraint = and(
                equals("country", "PL"),
                equals("channel", "mobile")
        );
        var context = ApplicabilityContext.of(Map.of(
                "country", "PL",
                "channel", "mobile"
        ));

        assertTrue(constraint.isSatisfiedBy(context));
    }

    @Test
    void shouldNotSatisfyAndConstraintWhenOneConstraintNotMet() {
        var constraint = and(
                equals("country", "PL"),
                equals("channel", "mobile")
        );
        var context = ApplicabilityContext.of(Map.of(
                "country", "PL",
                "channel", "web"
        ));

        assertFalse(constraint.isSatisfiedBy(context));
    }

    @Test
    void shouldSatisfyOrConstraintWhenAnyConstraintMet() {
        var constraint = or(
                equals("country", "PL"),
                equals("country", "UK")
        );
        var context = ApplicabilityContext.of(Map.of("country", "UK"));

        assertTrue(constraint.isSatisfiedBy(context));
    }

    @Test
    void shouldNotSatisfyOrConstraintWhenNoConstraintMet() {
        var constraint = or(
                equals("country", "PL"),
                equals("country", "UK")
        );
        var context = ApplicabilityContext.of(Map.of("country", "DE"));

        assertFalse(constraint.isSatisfiedBy(context));
    }

    @Test
    void shouldSatisfyNotConstraint() {
        var constraint = not(equals("country", "PL"));
        var context = ApplicabilityContext.of(Map.of("country", "UK"));

        assertTrue(constraint.isSatisfiedBy(context));
    }

    @Test
    void shouldNotSatisfyNotConstraint() {
        var constraint = not(equals("country", "PL"));
        var context = ApplicabilityContext.of(Map.of("country", "PL"));

        assertFalse(constraint.isSatisfiedBy(context));
    }

    @Test
    void shouldSatisfyComplexNestedConstraint() {
        // (country = PL OR country = UK) AND (channel = mobile OR channel = web) AND age < 16
        var constraint = and(
                or(equals("country", "PL"), equals("country", "UK")),
                or(equals("channel", "mobile"), equals("channel", "web")),
                lessThan("age", 16)
        );

        var context = ApplicabilityContext.of(Map.of(
                "country", "UK",
                "channel", "mobile",
                "age", "12"
        ));

        assertTrue(constraint.isSatisfiedBy(context));
    }

    @Test
    void shouldNotSatisfyComplexNestedConstraintWhenOnePartFails() {
        // Same constraint as above
        var constraint = and(
                or(equals("country", "PL"), equals("country", "UK")),
                or(equals("channel", "mobile"), equals("channel", "web")),
                lessThan("age", 16)
        );

        // Age is too high
        var context = ApplicabilityContext.of(Map.of(
                "country", "UK",
                "channel", "mobile",
                "age", "18"
        ));

        assertFalse(constraint.isSatisfiedBy(context));
    }

    @Test
    void shouldSatisfyAlwaysTrueConstraint() {
        var constraint = alwaysTrue();
        var context = ApplicabilityContext.empty();

        assertTrue(constraint.isSatisfiedBy(context));
    }

    @Test
    void shouldSatisfyAlwaysTrueConstraintWithAnyContext() {
        var constraint = alwaysTrue();
        var context = ApplicabilityContext.of(Map.of(
                "country", "PL",
                "channel", "mobile"
        ));

        assertTrue(constraint.isSatisfiedBy(context));
    }

    @Test
    void shouldUseApplicabilityConstraintInProductType() {
        var mobileOnlyProduct = ProductType.builder(
                        UuidProductIdentifier.random(),
                        ProductName.of("Mobile App Premium"),
                        ProductDescription.of("Premium feature available only on mobile"),
                        com.softwarearchetypes.quantity.Unit.pieces(),
                        ProductTrackingStrategy.IDENTICAL
                )
                .withApplicabilityConstraint(equals("channel", "mobile"))
                .build();

        var mobileContext = ApplicabilityContext.of(Map.of("channel", "mobile"));
        var webContext = ApplicabilityContext.of(Map.of("channel", "web"));

        assertTrue(mobileOnlyProduct.isApplicableFor(mobileContext));
        assertFalse(mobileOnlyProduct.isApplicableFor(webContext));
    }

    @Test
    void shouldUseComplexApplicabilityConstraintInProductType() {
        // Product only for PL/UK, on mobile/web, for users under 16
        var pediatricProduct = ProductType.builder(
                        UuidProductIdentifier.random(),
                        ProductName.of("Pediatric Service"),
                        ProductDescription.of("Service for children"),
                        com.softwarearchetypes.quantity.Unit.pieces(),
                        ProductTrackingStrategy.IDENTICAL
                )
                .withApplicabilityConstraint(
                        and(
                                or(equals("country", "PL"), equals("country", "UK")),
                                or(equals("channel", "mobile"), equals("channel", "web")),
                                lessThan("age", 16)
                        )
                )
                .build();

        var validContext = ApplicabilityContext.of(Map.of(
                "country", "PL",
                "channel", "mobile",
                "age", "10"
        ));

        var invalidContextAge = ApplicabilityContext.of(Map.of(
                "country", "PL",
                "channel", "mobile",
                "age", "18"
        ));

        var invalidContextCountry = ApplicabilityContext.of(Map.of(
                "country", "DE",
                "channel", "mobile",
                "age", "10"
        ));

        assertTrue(pediatricProduct.isApplicableFor(validContext));
        assertFalse(pediatricProduct.isApplicableFor(invalidContextAge));
        assertFalse(pediatricProduct.isApplicableFor(invalidContextCountry));
    }

    @Test
    void shouldUseDefaultAlwaysTrueConstraintWhenNotSpecified() {
        var product = ProductType.identical(
                UuidProductIdentifier.random(),
                ProductName.of("Universal Product"),
                ProductDescription.of("No restrictions"),
                com.softwarearchetypes.quantity.Unit.pieces()
        );

        var anyContext1 = ApplicabilityContext.empty();
        var anyContext2 = ApplicabilityContext.of(Map.of("country", "PL"));
        var anyContext3 = ApplicabilityContext.of(Map.of("channel", "mobile", "age", "99"));

        assertTrue(product.isApplicableFor(anyContext1));
        assertTrue(product.isApplicableFor(anyContext2));
        assertTrue(product.isApplicableFor(anyContext3));
    }

    @Test
    void shouldCombineConstraintsWithInOperator() {
        var constraint = and(
                in("country", "PL", "UK", "DE"),
                in("channel", "mobile", "web")
        );

        var validContext = ApplicabilityContext.of(Map.of(
                "country", "DE",
                "channel", "web"
        ));

        var invalidContext = ApplicabilityContext.of(Map.of(
                "country", "FR",
                "channel", "web"
        ));

        assertTrue(constraint.isSatisfiedBy(validContext));
        assertFalse(constraint.isSatisfiedBy(invalidContext));
    }

    @Test
    void shouldUseBetweenForRangeConstraints() {
        var ageRestrictedProduct = ProductType.builder(
                        UuidProductIdentifier.random(),
                        ProductName.of("Teen Product"),
                        ProductDescription.of("For teenagers only"),
                        com.softwarearchetypes.quantity.Unit.pieces(),
                        ProductTrackingStrategy.IDENTICAL
                )
                .withApplicabilityConstraint(between("age", 13, 19))
                .build();

        var validContext = ApplicabilityContext.of(Map.of("age", "15"));
        var tooYoung = ApplicabilityContext.of(Map.of("age", "10"));
        var tooOld = ApplicabilityContext.of(Map.of("age", "25"));

        assertTrue(ageRestrictedProduct.isApplicableFor(validContext));
        assertFalse(ageRestrictedProduct.isApplicableFor(tooYoung));
        assertFalse(ageRestrictedProduct.isApplicableFor(tooOld));
    }
}
