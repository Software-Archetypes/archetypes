package com.softwarearchetypes.pricing.formula;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jamsesso.jsonlogic.JsonLogic;
import io.github.jamsesso.jsonlogic.JsonLogicException;

import java.math.BigDecimal;
import java.util.Map;

class BasicFormula implements FormulaPricing {

    private final String formula;
    private final Class<?> inputDataType;

    public BasicFormula(String formula, String inputDataType) {
        this.formula = formula;
        this.inputDataType = ClassConverter.convertToEntityAttribute(inputDataType);
    }

    @Override
    public BigDecimal calculatePrice(Object data) {

        if (data.getClass() != inputDataType) {
            throw new IllegalArgumentException("The data provided has a different structure than expected. Expected data structure: " + inputDataType);
        }

        var objectMapper = new ObjectMapper();

        try {
            var dataJson = objectMapper.convertValue(data, Map.class);
            var functionResult = new JsonLogic().apply(formula, dataJson);

            return new BigDecimal(functionResult.toString());

        } catch (JsonLogicException e) {
            throw new IllegalArgumentException("Cannot execute formula", e);
        }
    }
}
