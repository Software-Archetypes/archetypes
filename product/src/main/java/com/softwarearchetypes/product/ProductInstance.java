package com.softwarearchetypes.product;

import com.softwarearchetypes.quantity.Quantity;

import java.util.Optional;

import static com.softwarearchetypes.common.Preconditions.checkArgument;

/**
 * ProductInstance represents a specific instance/exemplar of a ProductType.
 *
 * Examples:
 * - ProductType: "iPhone 15 Pro 256GB" -> ProductInstance: specific phone with serial ABC123
 * - ProductType: "Clean Code book" -> ProductInstance: specific book copy
 * - ProductType: "Organic Milk 1L" -> ProductInstance: specific bottle from batch LOT-2024-001
 * - ProductType: "Consulting" -> ProductInstance: 8.5 hours of consulting delivered
 *
 * Each instance must be tracked by at least one of:
 * - SerialNumber (individual tracking)
 * - Batch (group tracking for quality control)
 * - Or both (e.g., high-value items in batches like TVs)
 *
 * Optional quantity tracks the amount for this specific instance (e.g., 8.5 hours, 3.2 kg).
 * If not specified, quantity is implicitly 1 unit of the ProductType's preferred unit.
 *
 * ProductInstance can have features that specify the actual values for features
 * defined in the ProductType (e.g., color=red, size=L, yearOfProduction=2023).
 */
class ProductInstance {

    private final ProductInstanceId id;
    private final ProductType productType;
    private final SerialNumber serialNumber;
    private final BatchId batchId;
    private final Quantity quantity;
    private final ProductFeatureInstances features;

    private ProductInstance(ProductInstanceId id,
                           ProductType productType,
                           SerialNumber serialNumber,
                           BatchId batchId,
                           Quantity quantity,
                           ProductFeatureInstances features) {
        checkArgument(id != null, "ProductInstanceId must be defined");
        checkArgument(productType != null, "ProductType must be defined");
        checkArgument(features != null, "ProductFeatureInstances must be defined");

        validateTrackingRequirements(productType, serialNumber, batchId);
        validateQuantityUnit(productType, quantity);
        features.validateAgainst(productType.featureTypes());

        this.id = id;
        this.productType = productType;
        this.serialNumber = serialNumber;
        this.batchId = batchId;
        this.quantity = quantity;
        this.features = features;
    }

    private static void validateTrackingRequirements(ProductType productType,
                                                     SerialNumber serialNumber,
                                                     BatchId batchId) {
        checkArgument(serialNumber != null || batchId != null,
            "ProductInstance must have either SerialNumber or BatchId (or both)");

        ProductTrackingStrategy strategy = productType.trackingStrategy();

        if (strategy.isTrackedIndividually() && serialNumber == null) {
            throw new IllegalArgumentException(
                "ProductType requires individual tracking (strategy: " + strategy + ")"
            );
        }

        if (strategy.isTrackedByBatch() && batchId == null) {
            throw new IllegalArgumentException(
                "ProductType requires batch tracking (strategy: " + strategy + ")"
            );
        }

        if (strategy.requiresBothTrackingMethods() && (serialNumber == null || batchId == null)) {
            throw new IllegalArgumentException(
                "ProductType requires both individual and batch tracking (strategy: " + strategy + ")"
            );
        }
    }

    private static void validateQuantityUnit(ProductType productType, Quantity quantity) {
        if (quantity != null) {
            checkArgument(quantity.unit().equals(productType.preferredUnit()),
                "Quantity unit must match ProductType's preferred unit");
        }
    }

    static Builder builder() {
        return new Builder();
    }

    ProductInstanceId id() {
        return id;
    }

    ProductType productType() {
        return productType;
    }

    Optional<SerialNumber> serialNumber() {
        return Optional.ofNullable(serialNumber);
    }

    Optional<BatchId> batchId() {
        return Optional.ofNullable(batchId);
    }

    Optional<Quantity> quantity() {
        return Optional.ofNullable(quantity);
    }

    /**
     * Returns the effective quantity of this instance.
     * If explicit quantity is set, returns it.
     * Otherwise returns implicit "1 unit" of the product's preferred unit.
     */
    Quantity effectiveQuantity() {
        return quantity != null
            ? quantity
            : Quantity.of(1, productType.preferredUnit());
    }

    ProductFeatureInstances features() {
        return features;
    }

    @Override
    public String toString() {
        return "ProductInstance{id=%s, type=%s, serial=%s, batch=%s, quantity=%s, features=%s}".formatted(
            id,
            productType.name(),
            serialNumber != null ? serialNumber : "none",
            batchId != null ? batchId : "none",
            quantity != null ? quantity : "implicit 1 " + productType.preferredUnit(),
            features
        );
    }

    static class Builder {
        private ProductInstanceId id;
        private ProductType productType;
        private SerialNumber serialNumber;
        private BatchId batchId;
        private Quantity quantity;
        private final java.util.List<ProductFeatureInstance> features = new java.util.ArrayList<>();

        Builder id(ProductInstanceId id) {
            this.id = id;
            return this;
        }

        Builder type(ProductType type) {
            this.productType = type;
            return this;
        }

        Builder serial(SerialNumber serial) {
            this.serialNumber = serial;
            return this;
        }

        Builder batch(BatchId batch) {
            this.batchId = batch;
            return this;
        }

        Builder quantity(Quantity quantity) {
            this.quantity = quantity;
            return this;
        }

        Builder withFeature(ProductFeatureInstance feature) {
            this.features.add(feature);
            return this;
        }

        Builder withFeature(ProductFeatureType featureType, Object value) {
            this.features.add(new ProductFeatureInstance(featureType, value));
            return this;
        }

        ProductInstance build() {
            ProductFeatureInstances featureInstances = new ProductFeatureInstances(features);
            return new ProductInstance(id, productType, serialNumber, batchId, quantity, featureInstances);
        }
    }
}
