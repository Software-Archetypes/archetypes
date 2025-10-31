package com.softwarearchetypes.quantity;

import java.math.BigDecimal;

import static com.softwarearchetypes.common.Preconditions.checkArgument;

/**
 * Quantity represents an amount with a unit of measurement.
 * Examples: 100 kg, 500 liters, 1000 pieces, 25.5 m²
 */
public record Quantity(BigDecimal amount, Unit unit) {

    public Quantity {
        checkArgument(amount != null, "Amount cannot be null");
        checkArgument(unit != null, "Unit cannot be null");
        checkArgument(amount.compareTo(BigDecimal.ZERO) >= 0, "Amount cannot be negative");
    }

    public static Quantity of(BigDecimal amount, Unit unit) {
        return new Quantity(amount, unit);
    }

    public static Quantity of(double amount, Unit unit) {
        return new Quantity(BigDecimal.valueOf(amount), unit);
    }

    public static Quantity of(int amount, Unit unit) {
        return new Quantity(BigDecimal.valueOf(amount), unit);
    }

    public Quantity add(Quantity other) {
        checkArgument(this.unit.equals(other.unit),
                String.format("Cannot add quantities with different units: %s and %s", this.unit, other.unit));
        return new Quantity(this.amount.add(other.amount), this.unit);
    }

    public Quantity subtract(Quantity other) {
        checkArgument(this.unit.equals(other.unit),
                String.format("Cannot subtract quantities with different units: %s and %s", this.unit, other.unit));
        return new Quantity(this.amount.subtract(other.amount), this.unit);
    }

    @Override
    public String toString() {
        return amount + " " + unit;
    }
}
