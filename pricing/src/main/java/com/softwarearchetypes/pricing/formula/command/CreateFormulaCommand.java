package com.softwarearchetypes.pricing.formula.command;

import org.springframework.util.StringUtils;

import java.util.Objects;

public record CreateFormulaCommand(
        String name,
        String formula,
        Class<?> inputDataType
) {

    public CreateFormulaCommand {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("Name cannot be empty");
        }

        if (!StringUtils.hasText(formula)) {
            throw new IllegalArgumentException("Formula cannot be empty");
        }

        Objects.requireNonNull(inputDataType, "Input data type cannot be null");
    }
}
