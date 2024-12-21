package com.softwarearchetypes.pricing.domain;

import org.springframework.util.StringUtils;

import java.util.*;

public class RuleContext {

    private final String name; //For now name is optional
    private final Map<String, String> context;

    public RuleContext() {
        this.name = null;
        this.context = new HashMap<>();
    }

    public RuleContext(String name) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("If name should be empty please use dedicated constructor");
        }
        this.name = name;
        this.context = new HashMap<>();
    }

    public void addVariable(String name, Object value) {
        Objects.requireNonNull(name, "Variable name can not be null");
        Objects.requireNonNull(value, "Variable value can not be null");

        context.put(name, value.toString());
    }

    public Map<String, String> getContextVariables() {
        return Collections.unmodifiableMap(context);
    }

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }
}
