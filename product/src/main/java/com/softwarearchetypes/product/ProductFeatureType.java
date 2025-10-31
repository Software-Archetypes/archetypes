package com.softwarearchetypes.product;

import java.util.Objects;

import static com.softwarearchetypes.common.Preconditions.checkArgument;

/**
 * ProductFeatureType represents a type of feature (such as color) of a good or service
 * and defines the constraint on possible values.
 * <p>
 * Examples:
 * - "color" with allowed values: {red, blue, black, white}
 * - "size" with allowed values: {S, M, L, XL}
 * - "yearOfProduction" with numeric range: 2020-2024
 * - "expiryDate" with date range: 2024-01-01 to 2024-12-31
 * <p>
 * Each ProductFeatureType defines:
 * - A unique identifier (name)
 * - A constraint that defines the value type and validation rules
 * <p>
 * This archetype allows for flexible specification of product features without
 * having to create new attributes or subclasses for each feature type.
 */
class ProductFeatureType {

    private final String name;
    private final FeatureValueConstraint constraint;

    ProductFeatureType(String name, FeatureValueConstraint constraint) {
        checkArgument(name != null && !name.isBlank(), "Feature type name must be defined");
        checkArgument(constraint != null, "Constraint must be defined");

        this.name = name;
        this.constraint = constraint;
    }

    /**
     * Creates a ProductFeatureType with allowed text values.
     * Example: color with values {red, blue, green}
     */
    static ProductFeatureType withAllowedValues(String name, String... allowedValues) {
        return new ProductFeatureType(name, AllowedValuesConstraint.of(allowedValues));
    }

    /**
     * Creates a ProductFeatureType with a numeric range constraint.
     * Example: year of production between 2020 and 2024
     */
    static ProductFeatureType withNumericRange(String name, int min, int max) {
        return new ProductFeatureType(name, new NumericRangeConstraint(min, max));
    }

    /**
     * Creates a ProductFeatureType with a decimal range constraint.
     * Example: weight between 0.5 and 100.0
     */
    static ProductFeatureType withDecimalRange(String name, String min, String max) {
        return new ProductFeatureType(name, DecimalRangeConstraint.of(min, max));
    }

    /**
     * Creates a ProductFeatureType with a regex pattern constraint.
     * Example: product code matching "^[A-Z]{2}-\d{4}$"
     */
    static ProductFeatureType withRegex(String name, String pattern) {
        return new ProductFeatureType(name, new RegexConstraint(pattern));
    }

    /**
     * Creates a ProductFeatureType with a date range constraint.
     * Example: expiry date between 2024-01-01 and 2024-12-31
     */
    static ProductFeatureType withDateRange(String name, String from, String to) {
        return new ProductFeatureType(name, DateRangeConstraint.between(from, to));
    }

    /**
     * Creates a ProductFeatureType with no constraints (any value of the specified type is valid).
     * Example: any text for a free-form comment
     */
    static ProductFeatureType unconstrained(String name, FeatureValueType valueType) {
        return new ProductFeatureType(name, new Unconstrained(valueType));
    }

    /**
     * Creates a ProductFeatureType with a custom constraint.
     */
    static ProductFeatureType of(String name, FeatureValueConstraint constraint) {
        return new ProductFeatureType(name, constraint);
    }

    String name() {
        return name;
    }

    FeatureValueConstraint constraint() {
        return constraint;
    }

    /**
     * Validates whether the given value is valid for this feature type.
     *
     * @param value
     *         the value to validate
     *
     * @return true if the value is valid
     */
    boolean isValidValue(Object value) {
        return constraint.isValid(value);
    }

    /**
     * Validates that the given value is valid for this feature type.
     * Throws an exception if the value is invalid.
     *
     * @param value
     *         the value to validate
     *
     * @throws IllegalArgumentException
     *         if the value is not valid
     */
    void validateValue(Object value) {
        checkArgument(value != null, "Feature value must not be null");
        checkArgument(constraint.valueType().isInstance(value),
                String.format("Feature '%s' expects type %s but got %s",
                        name,
                        constraint.valueType().type().getSimpleName(),
                        value.getClass().getSimpleName()));
        checkArgument(isValidValue(value),
                String.format("Invalid value '%s' for feature '%s'. Expected: %s",
                        value, name, constraint.desc()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProductFeatureType that = (ProductFeatureType) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "ProductFeatureType{name='%s', constraint=%s}".formatted(name, constraint.desc());
    }
}
