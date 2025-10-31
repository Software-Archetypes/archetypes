package com.softwarearchetypes.quantity.money;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

public class Money implements Comparable<Money> {

    private final org.javamoney.moneta.Money money;

    // Private constructor
    private Money(org.javamoney.moneta.Money money) {
        this.money = money;
    }

    // Factory methods - PLN
    public static Money pln(int amount) {
        return new Money(org.javamoney.moneta.Money.of(amount, "PLN"));
    }

    public static Money pln(BigDecimal amount) {
        return new Money(org.javamoney.moneta.Money.of(amount, "PLN"));
    }

    public static Money pln(Number amount) {
        return new Money(org.javamoney.moneta.Money.of(amount, "PLN"));
    }

    public static Money zeroPln() {
        return pln(0);
    }

    public static Money onePln() {
        return pln(1);
    }

    // Static utility methods
    public static Money min(Money one, Money two) {
        return one.compareTo(two) <= 0 ? one : two;
    }

    public static Optional<Money> min(Set<Money> values) {
        return values
                .stream()
                .reduce(Money::min);
    }

    public static Money max(Money one, Money two) {
        return one.compareTo(two) <= 0 ? two : one;
    }

    public static Money abs(Money from) {
        return from.abs();
    }

    // Arithmetic operations
    public Money add(Money toAdd) {
        return new Money(this.money.add(toAdd.money));
    }

    public Money subtract(Money toSubtract) {
        return new Money(this.money.subtract(toSubtract.money));
    }

    public Money negate() {
        return new Money(this.money.negate());
    }

    public Money abs() {
        return new Money(money.abs());
    }

    public Money[] divideAndRemainder(BigDecimal divider) {
        org.javamoney.moneta.Money[] result = this.money.divideAndRemainder(divider);
        return new Money[] { new Money(result[0]), new Money(result[1]) };
    }

    // Comparison operations
    public boolean isZero() {
        return this.money.isZero();
    }

    public boolean isNegative() {
        return this.money.isNegative();
    }

    public boolean isGreaterThan(Money other) {
        return this.money.isGreaterThan(other.money);
    }

    public boolean isGreaterThanOrEqualTo(Money other) {
        return this.money.isGreaterThanOrEqualTo(other.money);
    }

    // Value extraction
    public BigDecimal value() {
        return money.getNumber().numberValue(BigDecimal.class);
    }

    // Comparable implementation
    @Override
    public int compareTo(@NotNull Money other) {
        return this.money.compareTo(other.money);
    }

    // Object overrides
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Money other = (Money) o;
        return Objects.equals(this.money, other.money);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(money);
    }

    @Override
    public String toString() {
        return money.getCurrency().getCurrencyCode() + " " + money.getNumberStripped().toPlainString();
    }

}
