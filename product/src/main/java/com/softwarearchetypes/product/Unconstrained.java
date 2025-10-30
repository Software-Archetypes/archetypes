package com.softwarearchetypes.product;

import static com.softwarearchetypes.common.Preconditions.checkArgument;

/**
 * No constraints - any value of the specified type is valid.
 * Example: any text for a free-form comment field
 *
 * Persistence config example: {} (empty)
 */
class Unconstrained implements FeatureValueConstraint {

    private final FeatureValueType valueType;

    Unconstrained(FeatureValueType valueType) {
        checkArgument(valueType != null, "Value type must be defined");
        this.valueType = valueType;
    }

    @Override
    public FeatureValueType valueType() {
        return valueType;
    }

    @Override
    public String type() {
        return "UNCONSTRAINED";
    }

    @Override
    public boolean isValid(Object value) {
        return valueType.isInstance(value);
    }

    @Override
    public String desc() {
        return "any " + valueType.name().toLowerCase();
    }
}
