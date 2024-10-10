package com.softwarearchetypes.pricing.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RuleContext {

    private final String name;
    private final Map<String, String> context;

    public RuleContext(String name) {
        this.name = name; //For now name is optional
        context = new HashMap<>();
    }

    public void addVariable(String name, Object value) {
        Objects.requireNonNull(name, "Variable name can not be null");
        Objects.requireNonNull(value, "Variable value can not be null");

        context.put(name, value.toString());
    }

    public Map<String, String> getContextVariables() {
        return Collections.unmodifiableMap(context);
    }
}
