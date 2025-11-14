package com.softwarearchetypes.product;

import java.util.List;

/**
 * Result of package validation - either success or failure with error messages.
 * Used to validate if selected products match package structure rules.
 */
record PackageValidationResult(boolean valid, List<String> errors) {

    static PackageValidationResult success() {
        return new PackageValidationResult(true, List.of());
    }

    static PackageValidationResult failure(String error) {
        return new PackageValidationResult(false, List.of(error));
    }

    static PackageValidationResult failure(List<String> errors) {
        return new PackageValidationResult(false, errors);
    }

    boolean isValid() {
        return valid;
    }
}
