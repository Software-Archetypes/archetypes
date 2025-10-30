package com.softwarearchetypes.product;

/**
 * Defines constraints on product feature values, including type validation and value validation.
 *
 * Each constraint:
 * - Specifies the expected value type (TEXT, INTEGER, DECIMAL, DATE, BOOLEAN)
 * - Has a string identifier for persistence/deserialization
 * - Validates whether a given value satisfies the constraint
 * - Provides conversion between objects and String representation for persistence
 * - Describes the constraint in human-readable form
 *
 * Implementations include:
 * - AllowedValuesConstraint: restricts to a set of allowed values
 * - NumericRangeConstraint: restricts integers to a range
 * - DecimalRangeConstraint: restricts decimals to a range
 * - RegexConstraint: validates text against a regex pattern
 * - DateRangeConstraint: restricts dates to a range
 * - UnconstrainedConstraint: accepts any value of the specified type
 */
interface FeatureValueConstraint {

    /**
     * Returns the type of values this constraint applies to.
     */
    FeatureValueType valueType();

    /**
     * Returns the constraint type identifier for persistence/deserialization.
     * Examples: "ALLOWED_VALUES", "NUMERIC_RANGE", "REGEX", "DATE_RANGE", "UNCONSTRAINED"
     */
    String type();

    /**
     * Validates whether the given value satisfies this constraint.
     * The value must be of the correct type (checked via valueType()) and meet
     * the constraint's specific requirements.
     *
     * @param value the value to validate
     * @return true if the value is valid, false otherwise
     */
    boolean isValid(Object value);

    /**
     * Returns a human-readable description of this constraint.
     * Example: "one of: {red, blue, green}" or "integer between 1 and 100"
     */
    String desc();

    /**
     * Converts the object to its String representation for persistence.
     * Uses the valueType's conversion logic.
     *
     * @param value the value to convert (must be valid)
     * @return String representation
     */
    default String toString(Object value) {
        return valueType().castTo(value);
    }

    /**
     * Converts a String representation to the object, applying validation.
     * Uses the valueType's conversion logic and then validates the result.
     *
     * @param value the String to convert
     * @return the converted and validated object
     * @throws IllegalArgumentException if the value cannot be parsed or is invalid
     */
    default Object fromString(String value) {
        Object casted = valueType().castFrom(value);
        if (!isValid(casted)) {
            throw new IllegalArgumentException(
                "Invalid value: '%s'. Expected: %s".formatted(value, desc())
            );
        }
        return casted;
    }
}
