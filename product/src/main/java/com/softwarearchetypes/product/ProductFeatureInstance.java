package com.softwarearchetypes.product;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import static com.softwarearchetypes.common.Preconditions.checkArgument;

/**
 * ProductFeatureInstance represents a specific feature (such as color) of a good or service
 * and its value (e.g., blue).
 *
 * Examples:
 * - color: "red"
 * - size: "L"
 * - yearOfProduction: 2023
 * - expiryDate: 2024-12-31
 *
 * Each ProductFeatureInstance:
 * - References its ProductFeatureType (which defines the constraint)
 * - Has a value that satisfies the type's constraint
 * - Can convert its value to/from String for persistence
 *
 * The value type is validated against the feature type's constraint at construction time,
 * ensuring that invalid product instances cannot be created.
 */
class ProductFeatureInstance {

    private final ProductFeatureType featureType;
    private final Object value;

    ProductFeatureInstance(ProductFeatureType featureType, Object value) {
        checkArgument(featureType != null, "ProductFeatureType must be defined");
        checkArgument(value != null, "Feature value must be defined");

        // Validate type and value against the constraint
        featureType.validateValue(value);

        this.featureType = featureType;
        this.value = value;
    }

    /**
     * Creates a ProductFeatureInstance with the given value.
     * The value is validated according to the feature type's constraint.
     */
    static ProductFeatureInstance of(ProductFeatureType featureType, Object value) {
        return new ProductFeatureInstance(featureType, value);
    }

    /**
     * Creates a ProductFeatureInstance from a String representation.
     * The String is parsed and validated according to the feature type's constraint.
     */
    static ProductFeatureInstance fromString(ProductFeatureType featureType, String stringValue) {
        checkArgument(featureType != null, "ProductFeatureType must be defined");
        checkArgument(stringValue != null, "String value must be defined");

        Object parsedValue = featureType.constraint().fromString(stringValue);
        return new ProductFeatureInstance(featureType, parsedValue);
    }

    ProductFeatureType featureType() {
        return featureType;
    }

    /**
     * Returns the feature value as an Object.
     * Use type-specific methods (asString(), asInt(), etc.) for type-safe access.
     */
    Object value() {
        return value;
    }

    /**
     * Returns the String representation of the value (for persistence).
     */
    String valueAsString() {
        return featureType.constraint().toString(value);
    }

    /**
     * Returns the value as a String (if it's a text feature).
     * @throws IllegalStateException if the value is not a String
     */
    String asString() {
        if (!(value instanceof String)) {
            throw new IllegalStateException(
                "Feature '%s' value is not a string (type: %s)".formatted(
                    featureType.name(), value.getClass().getSimpleName())
            );
        }
        return (String) value;
    }

    /**
     * Returns the value as an Integer (if it's an integer feature).
     * @throws IllegalStateException if the value is not an Integer
     */
    int asInt() {
        if (!(value instanceof Integer)) {
            throw new IllegalStateException(
                "Feature '%s' value is not an integer (type: %s)".formatted(
                    featureType.name(), value.getClass().getSimpleName())
            );
        }
        return (Integer) value;
    }

    /**
     * Returns the value as a BigDecimal (if it's a decimal feature).
     * @throws IllegalStateException if the value is not a BigDecimal
     */
    BigDecimal asDecimal() {
        if (!(value instanceof BigDecimal)) {
            throw new IllegalStateException(
                "Feature '%s' value is not a decimal (type: %s)".formatted(
                    featureType.name(), value.getClass().getSimpleName())
            );
        }
        return (BigDecimal) value;
    }

    /**
     * Returns the value as a LocalDate (if it's a date feature).
     * @throws IllegalStateException if the value is not a LocalDate
     */
    LocalDate asDate() {
        if (!(value instanceof LocalDate)) {
            throw new IllegalStateException(
                "Feature '%s' value is not a date (type: %s)".formatted(
                    featureType.name(), value.getClass().getSimpleName())
            );
        }
        return (LocalDate) value;
    }

    /**
     * Returns the value as a Boolean (if it's a boolean feature).
     * @throws IllegalStateException if the value is not a Boolean
     */
    boolean asBoolean() {
        if (!(value instanceof Boolean)) {
            throw new IllegalStateException(
                "Feature '%s' value is not a boolean (type: %s)".formatted(
                    featureType.name(), value.getClass().getSimpleName())
            );
        }
        return (Boolean) value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductFeatureInstance that = (ProductFeatureInstance) o;
        return Objects.equals(featureType, that.featureType) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(featureType, value);
    }

    @Override
    public String toString() {
        return "ProductFeatureInstance{%s=%s}".formatted(featureType.name(), value);
    }
}
