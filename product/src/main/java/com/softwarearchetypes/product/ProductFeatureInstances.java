package com.softwarearchetypes.product;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.softwarearchetypes.common.Preconditions.checkArgument;

/**
 * Container for all feature instances of a ProductInstance.
 * Provides convenient access to feature values by name.
 */
class ProductFeatureInstances {

    private final Map<String, ProductFeatureInstance> features;

    ProductFeatureInstances(Collection<ProductFeatureInstance> instances) {
        checkArgument(instances != null, "Feature instances must be defined");

        // Build map indexed by feature type name for fast lookup
        this.features = instances.stream()
            .collect(Collectors.toUnmodifiableMap(
                inst -> inst.featureType().name(),
                inst -> inst
            ));
    }

    static ProductFeatureInstances empty() {
        return new ProductFeatureInstances(Set.of());
    }

    static ProductFeatureInstances of(ProductFeatureInstance... instances) {
        return new ProductFeatureInstances(Set.of(instances));
    }

    /**
     * Returns the feature instance by feature name.
     * @return Optional containing the instance, or empty if not found
     */
    Optional<ProductFeatureInstance> get(String featureName) {
        return Optional.ofNullable(features.get(featureName));
    }

    /**
     * Checks if a feature with the given name exists.
     */
    boolean has(String featureName) {
        return features.containsKey(featureName);
    }

    /**
     * Returns all feature instances.
     */
    Collection<ProductFeatureInstance> all() {
        return features.values();
    }

    /**
     * Returns the number of feature instances.
     */
    int size() {
        return features.size();
    }

    boolean isEmpty() {
        return features.isEmpty();
    }

    /**
     * Validates that all mandatory features from ProductType are present.
     * @throws IllegalArgumentException if any mandatory feature is missing
     */
    void validateAgainst(ProductFeatureTypes featureTypes) {
        Set<ProductFeatureType> mandatoryFeatures = featureTypes.mandatoryFeatures();

        for (ProductFeatureType mandatory : mandatoryFeatures) {
            if (!has(mandatory.name())) {
                throw new IllegalArgumentException(
                    "Mandatory feature '%s' is missing".formatted(mandatory.name())
                );
            }
        }

        // Validate that all provided features are defined in ProductType
        for (String featureName : features.keySet()) {
            if (!featureTypes.has(featureName)) {
                throw new IllegalArgumentException(
                    "Feature '%s' is not defined in ProductType".formatted(featureName)
                );
            }
        }
    }

    @Override
    public String toString() {
        return "ProductFeatureInstances{%s}".formatted(
            features.values().stream()
                .map(f -> "%s=%s".formatted(f.featureType().name(), f.value()))
                .collect(Collectors.joining(", "))
        );
    }
}
