package com.softwarearchetypes.product;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.softwarearchetypes.common.Preconditions.checkArgument;

/**
 * Restricts text values to a predefined set of allowed values.
 * Example: color can be one of {red, blue, green}
 *
 * Persistence config example: {"allowedValues": ["red", "blue", "green"]}
 */
class AllowedValuesConstraint implements FeatureValueConstraint {

    private final Set<String> allowedValues;

    AllowedValuesConstraint(Set<String> allowedValues) {
        checkArgument(allowedValues != null && !allowedValues.isEmpty(),
            "Allowed values must not be empty");
        this.allowedValues = Collections.unmodifiableSet(new HashSet<>(allowedValues));
    }

    static AllowedValuesConstraint of(String... values) {
        checkArgument(values != null && values.length > 0,
            "Allowed values must not be empty");
        return new AllowedValuesConstraint(Set.of(values));
    }

    @Override
    public FeatureValueType valueType() {
        return FeatureValueType.TEXT;
    }

    @Override
    public String type() {
        return "ALLOWED_VALUES";
    }

    @Override
    public boolean isValid(Object value) {
        return value instanceof String && allowedValues.contains((String) value);
    }

    @Override
    public String desc() {
        return "one of: " + allowedValues;
    }

    Set<String> allowedValues() {
        return allowedValues;
    }
}
