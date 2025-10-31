package com.softwarearchetypes.quantity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class QuantityTest {

    @Test
    void shouldCreateQuantityFromBigDecimal() {
        //given
        BigDecimal amount = new BigDecimal("100.5");
        Unit unit = Unit.kilograms();

        //when
        Quantity quantity = Quantity.of(amount, unit);

        //then
        assertEquals(amount, quantity.amount());
        assertEquals(unit, quantity.unit());
    }

    @Test
    void shouldCreateQuantityFromDouble() {
        //given
        double amount = 50.75;
        Unit unit = Unit.liters();

        //when
        Quantity quantity = Quantity.of(amount, unit);

        //then
        assertEquals(new BigDecimal("50.75"), quantity.amount());
        assertEquals(unit, quantity.unit());
    }

    @Test
    void shouldCreateQuantityFromInt() {
        //given
        int amount = 1000;
        Unit unit = Unit.pieces();

        //when
        Quantity quantity = Quantity.of(amount, unit);

        //then
        assertEquals(new BigDecimal("1000"), quantity.amount());
        assertEquals(unit, quantity.unit());
    }

    @Test
    void shouldThrowExceptionWhenAmountIsNull() {
        //given
        BigDecimal amount = null;
        Unit unit = Unit.kilograms();

        //when & then
        assertThrows(IllegalArgumentException.class, () -> Quantity.of(amount, unit));
    }

    @Test
    void shouldThrowExceptionWhenUnitIsNull() {
        //given
        BigDecimal amount = new BigDecimal("100");
        Unit unit = null;

        //when & then
        assertThrows(IllegalArgumentException.class, () -> Quantity.of(amount, unit));
    }

    @Test
    void shouldThrowExceptionWhenAmountIsNegative() {
        //given
        BigDecimal amount = new BigDecimal("-10");
        Unit unit = Unit.kilograms();

        //when & then
        assertThrows(IllegalArgumentException.class, () -> Quantity.of(amount, unit));
    }

    @Test
    void shouldAllowZeroAmount() {
        //given
        BigDecimal amount = BigDecimal.ZERO;
        Unit unit = Unit.pieces();

        //when
        Quantity quantity = Quantity.of(amount, unit);

        //then
        assertEquals(BigDecimal.ZERO, quantity.amount());
    }

    @Test
    void shouldAddQuantitiesWithSameUnit() {
        //given
        Quantity first = Quantity.of(100, Unit.kilograms());
        Quantity second = Quantity.of(50, Unit.kilograms());

        //when
        Quantity result = first.add(second);

        //then
        assertEquals(new BigDecimal("150"), result.amount());
        assertEquals(Unit.kilograms(), result.unit());
    }

    @Test
    void shouldAddQuantitiesWithDecimalAmounts() {
        //given
        Quantity first = Quantity.of(10.5, Unit.liters());
        Quantity second = Quantity.of(5.25, Unit.liters());

        //when
        Quantity result = first.add(second);

        //then
        assertEquals(new BigDecimal("15.75"), result.amount());
        assertEquals(Unit.liters(), result.unit());
    }

    @Test
    void shouldThrowExceptionWhenAddingQuantitiesWithDifferentUnits() {
        //given
        Quantity kilograms = Quantity.of(100, Unit.kilograms());
        Quantity liters = Quantity.of(50, Unit.liters());

        //when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> kilograms.add(liters)
        );

        assertTrue(exception.getMessage().contains("different units"));
    }

    @Test
    void shouldSubtractQuantitiesWithSameUnit() {
        //given
        Quantity first = Quantity.of(100, Unit.kilograms());
        Quantity second = Quantity.of(30, Unit.kilograms());

        //when
        Quantity result = first.subtract(second);

        //then
        assertEquals(new BigDecimal("70"), result.amount());
        assertEquals(Unit.kilograms(), result.unit());
    }

    @Test
    void shouldSubtractQuantitiesWithDecimalAmounts() {
        //given
        Quantity first = Quantity.of(50.75, Unit.meters());
        Quantity second = Quantity.of(20.5, Unit.meters());

        //when
        Quantity result = first.subtract(second);

        //then
        assertEquals(new BigDecimal("30.25"), result.amount());
        assertEquals(Unit.meters(), result.unit());
    }

    @Test
    void shouldThrowExceptionWhenSubtractingQuantitiesWithDifferentUnits() {
        //given
        Quantity meters = Quantity.of(100, Unit.meters());
        Quantity hours = Quantity.of(5, Unit.hours());

        //when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> meters.subtract(hours)
        );

        assertTrue(exception.getMessage().contains("different units"));
    }

    @Test
    void shouldThrowExceptionWhenSubtractionResultsInNegative() {
        //given
        Quantity first = Quantity.of(50, Unit.pieces());
        Quantity second = Quantity.of(100, Unit.pieces());

        //when & then
        assertThrows(IllegalArgumentException.class, () -> first.subtract(second));
    }

    @Test
    void shouldBeEqualWhenQuantitiesHaveSameAmountAndUnit() {
        //given
        Quantity first = Quantity.of(100, Unit.kilograms());
        Quantity second = Quantity.of(100, Unit.kilograms());

        //when & then
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenQuantitiesHaveDifferentAmounts() {
        //given
        Quantity first = Quantity.of(100, Unit.kilograms());
        Quantity second = Quantity.of(50, Unit.kilograms());

        //when & then
        assertNotEquals(first, second);
    }

    @Test
    void shouldNotBeEqualWhenQuantitiesHaveDifferentUnits() {
        //given
        Quantity first = Quantity.of(100, Unit.kilograms());
        Quantity second = Quantity.of(100, Unit.liters());

        //when & then
        assertNotEquals(first, second);
    }

    @Test
    void shouldNotBeEqualToNull() {
        //given
        Quantity quantity = Quantity.of(100, Unit.pieces());

        //when & then
        assertNotEquals(null, quantity);
    }

    @Test
    void shouldBeEqualToItself() {
        //given
        Quantity quantity = Quantity.of(100, Unit.kilograms());

        //when & then
        assertEquals(quantity, quantity);
    }

    @Test
    void shouldHaveProperStringRepresentation() {
        //given
        Quantity quantity = Quantity.of(100.5, Unit.kilograms());

        //when
        String result = quantity.toString();

        //then
        assertEquals("100.5 kg", result);
    }

    @Test
    void shouldHandleComplexUnitSymbolsInStringRepresentation() {
        //given
        Quantity quantity = Quantity.of(25.5, Unit.squareMeters());

        //when
        String result = quantity.toString();

        //then
        assertEquals("25.5 m²", result);
    }

    @Test
    void shouldHandleZeroInArithmeticOperations() {
        //given
        Quantity quantity = Quantity.of(100, Unit.kilograms());
        Quantity zero = Quantity.of(0, Unit.kilograms());

        //when
        Quantity addResult = quantity.add(zero);
        Quantity subtractResult = quantity.subtract(zero);

        //then
        assertEquals(quantity, addResult);
        assertEquals(quantity, subtractResult);
    }

    @Test
    void shouldHandleLargeNumbers() {
        //given
        BigDecimal largeAmount = new BigDecimal("9999999999999.99");
        Unit unit = Unit.pieces();

        //when
        Quantity quantity = Quantity.of(largeAmount, unit);

        //then
        assertEquals(largeAmount, quantity.amount());
        assertEquals(unit, quantity.unit());
    }

    @Test
    void shouldHandleVerySmallDecimals() {
        //given
        BigDecimal smallAmount = new BigDecimal("0.000001");
        Unit unit = Unit.kilograms();

        //when
        Quantity quantity = Quantity.of(smallAmount, unit);

        //then
        assertEquals(smallAmount, quantity.amount());
    }

    @Test
    void shouldPreservePrecisionInArithmeticOperations() {
        //given
        Quantity first = Quantity.of(new BigDecimal("10.123456789"), Unit.meters());
        Quantity second = Quantity.of(new BigDecimal("5.987654321"), Unit.meters());

        //when
        Quantity addResult = first.add(second);
        Quantity subtractResult = first.subtract(second);

        //then
        assertEquals(new BigDecimal("16.111111110"), addResult.amount());
        assertEquals(new BigDecimal("4.135802468"), subtractResult.amount());
    }

    @Test
    void shouldWorkWithAllPredefinedUnits() {
        //when
        Quantity pieces = Quantity.of(100, Unit.pieces());
        Quantity kilograms = Quantity.of(50.5, Unit.kilograms());
        Quantity liters = Quantity.of(25.75, Unit.liters());
        Quantity meters = Quantity.of(10, Unit.meters());
        Quantity squareMeters = Quantity.of(100, Unit.squareMeters());
        Quantity cubicMeters = Quantity.of(5, Unit.cubicMeters());
        Quantity hours = Quantity.of(8, Unit.hours());
        Quantity minutes = Quantity.of(30, Unit.minutes());

        //then
        assertNotNull(pieces);
        assertNotNull(kilograms);
        assertNotNull(liters);
        assertNotNull(meters);
        assertNotNull(squareMeters);
        assertNotNull(cubicMeters);
        assertNotNull(hours);
        assertNotNull(minutes);
    }

    @Test
    void shouldWorkWithCustomUnits() {
        //given
        Unit customUnit = Unit.of("widget", "widgets");
        Quantity quantity = Quantity.of(42, customUnit);

        //when
        String result = quantity.toString();

        //then
        assertEquals("42 widget", result);
        assertEquals(new BigDecimal("42"), quantity.amount());
        assertEquals(customUnit, quantity.unit());
    }

    @Test
    void shouldMaintainImmutability() {
        //given
        Quantity original = Quantity.of(100, Unit.kilograms());
        Quantity toAdd = Quantity.of(50, Unit.kilograms());

        //when
        Quantity result = original.add(toAdd);

        //then
        assertEquals(new BigDecimal("100"), original.amount());
        assertEquals(new BigDecimal("150"), result.amount());
        assertNotSame(original, result);
    }
}
