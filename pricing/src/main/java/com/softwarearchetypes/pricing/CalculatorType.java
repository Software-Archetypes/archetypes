package com.softwarearchetypes.pricing;

import java.util.Set;

public enum CalculatorType {


    SIMPLE_FIXED("simple-fixed", "Fixed amount calculator - returns %s PLN regardless", Set.of("amount"), Set.of()),
    SIMPLE_INTEREST("simple-interest", "Annual interest calculator - calculates %s%% annual interest based on base and time unit", Set.of("annualRate"), Set.of("base", "unit"));

    private final String typeName;
    private final String descriptionTemplate;
    private final Set<String> requiredCreationFields;
    private final Set<String> requiredCalculationFields;

    CalculatorType(String typeName, String descriptionTemplate, Set<String> requiredCreationFields, Set<String> requiredCalculationFields) {
        this.typeName = typeName;
        this.descriptionTemplate = descriptionTemplate;
        this.requiredCreationFields = requiredCreationFields;
        this.requiredCalculationFields = requiredCalculationFields;
    }

    public String getTypeName() {
        return typeName;
    }

    public String formatDescription(Object value) {
        return String.format(descriptionTemplate, value);
    }

    public Set<String> requiredCreationFields() {
        return requiredCreationFields;
    }

    Set<String> requiredCalculationFields() {
        return requiredCalculationFields;
    }

}