package com.softwarearchetypes.product;

import java.util.Map;
import java.util.Optional;

/**
 * Context for evaluating applicability constraints.
 * Generic key-value map representing the situation in which we check applicability.
 */
public class ApplicabilityContext {

    private final Map<String, String> parameters;

    private ApplicabilityContext(Map<String, String> parameters) {
        this.parameters = Map.copyOf(parameters);
    }

    public static ApplicabilityContext empty() {
        return new ApplicabilityContext(Map.of());
    }

    public static ApplicabilityContext of(Map<String, String> parameters) {
        return new ApplicabilityContext(parameters != null ? parameters : Map.of());
    }

    public Optional<String> get(String key) {
        return Optional.ofNullable(parameters.get(key));
    }

    public String getOrDefault(String key, String defaultValue) {
        return parameters.getOrDefault(key, defaultValue);
    }

    public boolean has(String key) {
        return parameters.containsKey(key);
    }

    public Map<String, String> asMap() {
        return parameters;
    }

    @Override
    public String toString() {
        return "ApplicabilityContext" + parameters;
    }
}
