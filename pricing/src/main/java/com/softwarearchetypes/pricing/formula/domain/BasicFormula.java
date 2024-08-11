package com.softwarearchetypes.pricing.formula.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jamsesso.jsonlogic.JsonLogic;
import io.github.jamsesso.jsonlogic.JsonLogicException;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

class BasicFormula implements FormulaPricing {

    private final String formula;
    private final Class<?> inputDataType;

    BasicFormula(String formula, Class<?> inputDataType) {
        this.formula = formula;
        this.inputDataType = inputDataType;
    }

    @Override
    public BigDecimal calculatePrice(Object data) throws PriceCalculationException {

        if (data.getClass() != inputDataType) {
            throw new IllegalArgumentException("The data provided has a different structure than expected. Expected data structure: " + inputDataType);
        }

        var objectMapper = new ObjectMapper();

        try {
            var dataJson = objectMapper.convertValue(data, Map.class);

            return Optional.ofNullable(new JsonLogic().apply(formula, dataJson))
                    .map(functionResult -> new BigDecimal(functionResult.toString()))
                    .orElseThrow(() -> new PriceCalculationException("It was not possible to calculate the price for the given parameters."));

        } catch (JsonLogicException e) {
            throw new PriceCalculationException("Error during formula execution", e);
        }
    }
}
