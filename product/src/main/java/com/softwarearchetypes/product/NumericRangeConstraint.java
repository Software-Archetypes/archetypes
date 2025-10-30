package com.softwarearchetypes.product;

import static com.softwarearchetypes.common.Preconditions.checkArgument;

/**
 * Restricts integer values to a numeric range [min, max].
 * Example: year of production between 2020 and 2024
 *
 * Persistence config example: {"min": 2020, "max": 2024}
 */
class NumericRangeConstraint implements FeatureValueConstraint {

    private final int min;
    private final int max;

    NumericRangeConstraint(int min, int max) {
        checkArgument(min <= max, "min must be less than or equal to max");
        this.min = min;
        this.max = max;
    }

    @Override
    public FeatureValueType valueType() {
        return FeatureValueType.INTEGER;
    }

    @Override
    public String type() {
        return "NUMERIC_RANGE";
    }

    @Override
    public boolean isValid(Object value) {
        if (!(value instanceof Integer)) {
            return false;
        }
        int intValue = (Integer) value;
        return intValue >= min && intValue <= max;
    }

    @Override
    public String desc() {
        return "integer between %d and %d".formatted(min, max);
    }

    int min() {
        return min;
    }

    int max() {
        return max;
    }
}
