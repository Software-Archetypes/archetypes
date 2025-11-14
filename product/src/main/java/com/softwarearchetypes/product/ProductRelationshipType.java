package com.softwarearchetypes.product;

/**
 * Wszystkie relacje są asymetryczne (skierowane): from → to
 */
public enum ProductRelationshipType {

    UPGRADABLE_TO,
    SUBSTITUTED_BY,
    REPLACED_BY,
    COMPLEMENTED_BY,
    COMPATIBLE_WITH,
    INCOMPATIBLE_WITH
}
