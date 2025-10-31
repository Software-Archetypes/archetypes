package com.softwarearchetypes.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;

import com.softwarearchetypes.quantity.money.Money;

import static java.math.BigDecimal.valueOf;

interface Calculator {
    Money calculate(Parameters parameters);

    String describe();

    CalculatorType getType();

    CalculatorId getId();

    String name();
}

record SimpleFixedCalculator(CalculatorId id, String name, BigDecimal amount) implements Calculator {

    public SimpleFixedCalculator(String name, BigDecimal amount) {
        this(CalculatorId.generate(), name, amount);
    }

    @Override
    public Money calculate(Parameters parameters) {
        return Money.pln(amount);
    }

    @Override
    public String describe() {
        return getType().formatDescription(amount);
    }

    @Override
    public CalculatorType getType() {
        return CalculatorType.SIMPLE_FIXED;
    }

    @Override
    public CalculatorId getId() {
        return id;
    }
}


record SimpleInterestCalculator(CalculatorId id, String name, BigDecimal annualRate) implements Calculator {

    private static final int SCALE = 10;

    public SimpleInterestCalculator(String name, BigDecimal annualRate) {
        this(CalculatorId.generate(), name, annualRate);
    }

    @Override
    public Money calculate(Parameters parameters) {
        if (!parameters.containsAll(getType().requiredCalculationFields())) {
            throw new IllegalArgumentException("SimpleInterestCalculator requiredPreviousKeys %s parameters".formatted(getType().requiredCalculationFields()));
        }

        Money base = (Money) parameters.get("base");
        ChronoUnit unit = (ChronoUnit) parameters.get("unit");

        BigDecimal rate = annualRate.divide(valueOf(100), SCALE, RoundingMode.HALF_UP);
        BigDecimal unitRate = rate.divide(unitsPerYear(unit), SCALE, RoundingMode.HALF_UP);
        BigDecimal baseAmount = base.value();

        return Money.pln(baseAmount.multiply(unitRate).setScale(2, RoundingMode.HALF_UP));
    }

    @Override
    public String describe() {
        return getType().formatDescription(annualRate);
    }

    @Override
    public CalculatorType getType() {
        return CalculatorType.SIMPLE_INTEREST;
    }

    @Override
    public CalculatorId getId() {
        return id;
    }

    private BigDecimal unitsPerYear(ChronoUnit unit) {
        return switch (unit) {
            case DAYS -> valueOf(365);
            case WEEKS -> valueOf(52);
            case MONTHS -> valueOf(12);
            case YEARS -> valueOf(1);
            default -> throw new IllegalArgumentException("Unsupported unit for annual calculation: " + unit);
        };
    }
}