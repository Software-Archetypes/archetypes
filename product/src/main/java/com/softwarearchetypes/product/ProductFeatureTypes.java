package com.softwarearchetypes.product;

import java.util.*;
import java.util.stream.Collectors;

import static com.softwarearchetypes.common.Preconditions.checkArgument;

/**
 * Container for all feature type definitions of a ProductType.
 * Provides convenient access to features by name and filtering by mandatory/optional status.
 */
class ProductFeatureTypes {

    private final Map<String, ProductFeatureTypeDefinition> features;

    ProductFeatureTypes(Collection<ProductFeatureTypeDefinition> definitions) {
        checkArgument(definitions != null, "Feature definitions must be defined");

        // Build map indexed by feature type name for fast lookup
        this.features = definitions.stream()
            .collect(Collectors.toUnmodifiableMap(
                def -> def.featureType().name(),
                def -> def
            ));
    }

    static ProductFeatureTypes empty() {
        return new ProductFeatureTypes(List.of());
    }

    static ProductFeatureTypes of(ProductFeatureTypeDefinition... definitions) {
        return new ProductFeatureTypes(List.of(definitions));
    }

    /**
     * Returns the feature type definition by name.
     * @return Optional containing the definition, or empty if not found
     */
    Optional<ProductFeatureTypeDefinition> get(String featureName) {
        return Optional.ofNullable(features.get(featureName));
    }

    /**
     * Returns the feature type by name.
     * @return Optional containing the feature type, or empty if not found
     */
    Optional<ProductFeatureType> getFeatureType(String featureName) {
        return get(featureName).map(ProductFeatureTypeDefinition::featureType);
    }

    /**
     * Checks if a feature with the given name exists.
     */
    boolean has(String featureName) {
        return features.containsKey(featureName);
    }

    /**
     * Checks if a feature is mandatory.
     * @return true if the feature exists and is mandatory, false otherwise
     */
    boolean isMandatory(String featureName) {
        return get(featureName)
            .map(ProductFeatureTypeDefinition::isMandatory)
            .orElse(false);
    }

    /**
     * Returns all mandatory feature types.
     */
    Set<ProductFeatureType> mandatoryFeatures() {
        return features.values().stream()
            .filter(ProductFeatureTypeDefinition::isMandatory)
            .map(ProductFeatureTypeDefinition::featureType)
            .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Returns all optional feature types.
     */
    Set<ProductFeatureType> optionalFeatures() {
        return features.values().stream()
            .filter(ProductFeatureTypeDefinition::isOptional)
            .map(ProductFeatureTypeDefinition::featureType)
            .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Returns all feature types (both mandatory and optional).
     */
    Set<ProductFeatureType> allFeatures() {
        return features.values().stream()
            .map(ProductFeatureTypeDefinition::featureType)
            .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Returns the number of feature types.
     */
    int size() {
        return features.size();
    }

    boolean isEmpty() {
        return features.isEmpty();
    }

    @Override
    public String toString() {
        return "ProductFeatureTypes{mandatory=%d, optional=%d}".formatted(
            mandatoryFeatures().size(),
            optionalFeatures().size()
        );
    }
}
