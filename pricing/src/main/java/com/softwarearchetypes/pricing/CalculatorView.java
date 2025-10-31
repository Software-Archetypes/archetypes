package com.softwarearchetypes.pricing;

public record CalculatorView(CalculatorId calculatorId, String name, CalculatorType type, String description) {

    static CalculatorView from(Calculator calc) {
        return new CalculatorView(calc.getId(), calc.name(), calc.getType(), calc.describe());
    }
}