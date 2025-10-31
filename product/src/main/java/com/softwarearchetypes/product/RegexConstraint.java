package com.softwarearchetypes.product;

import java.util.regex.Pattern;

import static com.softwarearchetypes.common.Preconditions.checkArgument;

/**
 * Validates text values against a regular expression pattern.
 * Example: product code must match pattern "^[A-Z]{2}-\d{4}$"
 *
 * Persistence config example: {"pattern": "^[A-Z]{2}-\\d{4}$"}
 */
class RegexConstraint implements FeatureValueConstraint {

    private final Pattern pattern;
    private final String patternString;

    RegexConstraint(String pattern) {
        checkArgument(pattern != null && !pattern.isBlank(), "Pattern must be defined");
        this.patternString = pattern;
        this.pattern = Pattern.compile(pattern);
    }

    static FeatureValueConstraint of(String pattern) {
        return new RegexConstraint(pattern);
    }

    @Override
    public FeatureValueType valueType() {
        return FeatureValueType.TEXT;
    }

    @Override
    public String type() {
        return "REGEX";
    }

    @Override
    public boolean isValid(Object value) {
        if (!(value instanceof String)) {
            return false;
        }
        return pattern.matcher((String) value).matches();
    }

    @Override
    public String desc() {
        return "text matching pattern: " + patternString;
    }

    String pattern() {
        return patternString;
    }
}
