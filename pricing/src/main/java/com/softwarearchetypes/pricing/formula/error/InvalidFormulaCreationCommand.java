package com.softwarearchetypes.pricing.formula.error;

public record InvalidFormulaCreationCommand(
        String error
) {


    public static InvalidFormulaCreationCommand dueToErrorDuringParsingInputDataType() {
        return new InvalidFormulaCreationCommand("Error occurred during parsing to JSON input data type");
    }

}
