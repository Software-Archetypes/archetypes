package com.softwarearchetypes.quantity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnitTest {

    @Test
    void shouldCreateUnitWithSymbolAndName() {
        //given
        String symbol = "kg";
        String name = "kilograms";

        //when
        Unit unit = Unit.of(symbol, name);

        //then
        assertEquals(symbol, unit.symbol());
        assertEquals(name, unit.name());
    }

    @Test
    void shouldThrowExceptionWhenSymbolIsNull() {
        //given
        String symbol = null;
        String name = "kilograms";

        //when & then
        assertThrows(IllegalArgumentException.class, () -> Unit.of(symbol, name));
    }

    @Test
    void shouldThrowExceptionWhenSymbolIsBlank() {
        //given
        String symbol = "   ";
        String name = "kilograms";

        //when & then
        assertThrows(IllegalArgumentException.class, () -> Unit.of(symbol, name));
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        //given
        String symbol = "kg";
        String name = null;

        //when & then
        assertThrows(IllegalArgumentException.class, () -> Unit.of(symbol, name));
    }

    @Test
    void shouldThrowExceptionWhenNameIsBlank() {
        //given
        String symbol = "kg";
        String name = "";

        //when & then
        assertThrows(IllegalArgumentException.class, () -> Unit.of(symbol, name));
    }

    @Test
    void shouldCreatePiecesUnit() {
        //when
        Unit unit = Unit.pieces();

        //then
        assertEquals("pcs", unit.symbol());
        assertEquals("pieces", unit.name());
    }

    @Test
    void shouldCreateKilogramsUnit() {
        //when
        Unit unit = Unit.kilograms();

        //then
        assertEquals("kg", unit.symbol());
        assertEquals("kilograms", unit.name());
    }

    @Test
    void shouldCreateLitersUnit() {
        //when
        Unit unit = Unit.liters();

        //then
        assertEquals("l", unit.symbol());
        assertEquals("liters", unit.name());
    }

    @Test
    void shouldCreateMetersUnit() {
        //when
        Unit unit = Unit.meters();

        //then
        assertEquals("m", unit.symbol());
        assertEquals("meters", unit.name());
    }

    @Test
    void shouldCreateSquareMetersUnit() {
        //when
        Unit unit = Unit.squareMeters();

        //then
        assertEquals("m²", unit.symbol());
        assertEquals("square meters", unit.name());
    }

    @Test
    void shouldCreateCubicMetersUnit() {
        //when
        Unit unit = Unit.cubicMeters();

        //then
        assertEquals("m³", unit.symbol());
        assertEquals("cubic meters", unit.name());
    }

    @Test
    void shouldCreateHoursUnit() {
        //when
        Unit unit = Unit.hours();

        //then
        assertEquals("h", unit.symbol());
        assertEquals("hours", unit.name());
    }

    @Test
    void shouldCreateMinutesUnit() {
        //when
        Unit unit = Unit.minutes();

        //then
        assertEquals("min", unit.symbol());
        assertEquals("minutes", unit.name());
    }

    @Test
    void shouldBeEqualWhenUnitsHaveSameSymbolAndName() {
        //given
        Unit first = Unit.of("kg", "kilograms");
        Unit second = Unit.of("kg", "kilograms");

        //when & then
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenUnitsHaveDifferentSymbols() {
        //given
        Unit first = Unit.of("kg", "kilograms");
        Unit second = Unit.of("g", "grams");

        //when & then
        assertNotEquals(first, second);
    }

    @Test
    void shouldNotBeEqualWhenUnitsHaveDifferentNames() {
        //given
        Unit first = Unit.of("m", "meters");
        Unit second = Unit.of("m", "miles");

        //when & then
        assertNotEquals(first, second);
    }

    @Test
    void shouldNotBeEqualToNull() {
        //given
        Unit unit = Unit.pieces();

        //when & then
        assertNotEquals(null, unit);
    }

    @Test
    void shouldBeEqualToItself() {
        //given
        Unit unit = Unit.kilograms();

        //when & then
        assertEquals(unit, unit);
    }

    @Test
    void shouldReturnSymbolAsStringRepresentation() {
        //given
        Unit unit = Unit.of("kg", "kilograms");

        //when
        String result = unit.toString();

        //then
        assertEquals("kg", result);
    }

    @Test
    void shouldReturnSymbolForComplexUnits() {
        //given
        Unit unit = Unit.squareMeters();

        //when
        String result = unit.toString();

        //then
        assertEquals("m²", result);
    }

    @Test
    void shouldCreateCustomUnit() {
        //given
        String symbol = "℃";
        String name = "degrees Celsius";

        //when
        Unit unit = Unit.of(symbol, name);

        //then
        assertEquals(symbol, unit.symbol());
        assertEquals(name, unit.name());
        assertEquals(symbol, unit.toString());
    }

    @Test
    void shouldHandleUnicodeSymbols() {
        //given
        String symbol = "Ω";
        String name = "ohm";

        //when
        Unit unit = Unit.of(symbol, name);

        //then
        assertEquals(symbol, unit.symbol());
        assertEquals(name, unit.name());
    }
}
