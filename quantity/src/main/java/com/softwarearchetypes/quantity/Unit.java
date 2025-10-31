package com.softwarearchetypes.quantity;

import static com.softwarearchetypes.common.Preconditions.checkArgument;

/**
 * Unit of measurement for quantities.
 * Examples: kg, l, pcs, m3, m2, hours, etc.
 */
public record Unit(String symbol, String name) {

    public Unit {
        checkArgument(symbol != null && !symbol.isBlank(), "Unit symbol cannot be null or blank");
        checkArgument(name != null && !name.isBlank(), "Unit name cannot be null or blank");
    }

    public static Unit of(String symbol, String name) {
        return new Unit(symbol, name);
    }

    // Common units
    public static Unit pieces() {
        return new Unit("pcs", "pieces");
    }

    public static Unit kilograms() {
        return new Unit("kg", "kilograms");
    }

    public static Unit liters() {
        return new Unit("l", "liters");
    }

    public static Unit meters() {
        return new Unit("m", "meters");
    }

    public static Unit squareMeters() {
        return new Unit("m²", "square meters");
    }

    public static Unit cubicMeters() {
        return new Unit("m³", "cubic meters");
    }

    public static Unit hours() {
        return new Unit("h", "hours");
    }

    public static Unit minutes() {
        return new Unit("min", "minutes");
    }

    @Override
    public String toString() {
        return symbol;
    }
}
