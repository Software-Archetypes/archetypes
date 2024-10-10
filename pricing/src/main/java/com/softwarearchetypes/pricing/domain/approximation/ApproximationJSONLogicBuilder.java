package com.softwarearchetypes.pricing.domain.approximation;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;

class ApproximationJSONLogicBuilder {

    private static final String MAP_KEY = "key";
    private final JSONArray ifArray;

    public ApproximationJSONLogicBuilder() {
        ifArray = new JSONArray();
    }

    public ApproximationJSONLogicBuilder addIfCondition(BigDecimal key, String valueFormula) {
        ifArray.put(new JSONObject().put("==", new JSONArray().put(new JSONObject().put("var", MAP_KEY)).put(key)));
        ifArray.put(valueFormula);
        return this;
    }

    public String build() {
        return new JSONObject().put("if", ifArray).toString();
    }
}
