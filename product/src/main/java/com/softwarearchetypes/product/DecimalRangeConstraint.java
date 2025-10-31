package com.softwarearchetypes.product;

import java.math.BigDecimal;

import static com.softwarearchetypes.common.Preconditions.checkArgument;

/**
 * Restricts decimal values to a numeric range [min, max].
 * Example: weight between 0.5 and 100.0 kg
 *
 * Persistence config example: {"min": "0.5", "max": "100.0"}
 */
class DecimalRangeConstraint implements FeatureValueConstraint {

    private final BigDecimal min;
    private final BigDecimal max;

    DecimalRangeConstraint(BigDecimal min, BigDecimal max) {
        checkArgument(min != null, "min must be defined");
        checkArgument(max != null, "max must be defined");
        checkArgument(min.compareTo(max) <= 0, "min must be less than or equal to max");
        this.min = min;
        this.max = max;
    }

    static DecimalRangeConstraint of(String min, String max) {
        return new DecimalRangeConstraint(new BigDecimal(min), new BigDecimal(max));
    }

    static FeatureValueConstraint between(BigDecimal min, BigDecimal max) {
        return new DecimalRangeConstraint(min, max);
    }

    @Override
    public FeatureValueType valueType() {
        return FeatureValueType.DECIMAL;
    }

    @Override
    public String type() {
        return "DECIMAL_RANGE";
    }

    @Override
    public boolean isValid(Object value) {
        if (!(value instanceof BigDecimal)) {
            return false;
        }
        BigDecimal decimalValue = (BigDecimal) value;
        return decimalValue.compareTo(min) >= 0 && decimalValue.compareTo(max) <= 0;
    }

    @Override
    public String desc() {
        return "decimal between %s and %s".formatted(min, max);
    }

    BigDecimal min() {
        return min;
    }

    BigDecimal max() {
        return max;
    }
}
