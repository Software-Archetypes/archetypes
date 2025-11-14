package com.softwarearchetypes.product;

import com.softwarearchetypes.quantity.Quantity;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for creating both ProductInstance and PackageInstance with fluent API.
 * <p>
 * Common attributes (id, serial, batch) are set in the main builder.
 * Type-specific attributes are set in specialized inner builders returned by asProductInstance() or asPackageInstance().
 * <p>
 * Usage:
 * <pre>
 * ProductInstance instance = new InstanceBuilder(InstanceId.newOne())
 *     .withSerial(SerialNumber.of("ABC123"))
 *     .asProductInstance(laptopType)
 *         .withQuantity(Quantity.of(1, Unit.pieces()))
 *         .withFeature(colorFeature, "silver")
 *         .build();
 *
 * PackageInstance packageInstance = new InstanceBuilder(InstanceId.newOne())
 *     .withBatch(BatchId.of("BATCH-001"))
 *     .asPackageInstance(bundleType)
 *         .withSelection(List.of(
 *             new SelectedInstance(ram16GBInstance, 1),
 *             new SelectedInstance(ssd512GBInstance, 1)
 *         ))
 *         .build();
 * </pre>
 */
class InstanceBuilder {
    // Common fields - required upfront
    private final InstanceId id;

    // Common fields - optional
    private SerialNumber serialNumber;
    private BatchId batchId;

    InstanceBuilder(InstanceId id) {
        this.id = id;
    }

    /**
     * Sets serial number for the instance.
     */
    public InstanceBuilder withSerial(SerialNumber serialNumber) {
        this.serialNumber = serialNumber;
        return this;
    }

    /**
     * Sets batch ID for the instance.
     */
    public InstanceBuilder withBatch(BatchId batchId) {
        this.batchId = batchId;
        return this;
    }

    /**
     * Starts building a ProductInstance.
     * Returns specialized builder for ProductInstance-specific attributes.
     */
    public ProductInstanceBuilder asProductInstance(ProductType productType) {
        return new ProductInstanceBuilder(productType);
    }

    /**
     * Starts building a PackageInstance.
     * Returns specialized builder for PackageInstance-specific attributes.
     */
    public PackageInstanceBuilder asPackageInstance(PackageType packageType) {
        return new PackageInstanceBuilder(packageType);
    }

    /**
     * Specialized builder for ProductInstance.
     * Has access to common fields from outer InstanceBuilder.
     */
    public class ProductInstanceBuilder {
        private final ProductType productType;
        private Quantity quantity;
        private final List<ProductFeatureInstance> features = new ArrayList<>();

        ProductInstanceBuilder(ProductType productType) {
            this.productType = productType;
        }

        public ProductInstanceBuilder withQuantity(Quantity quantity) {
            this.quantity = quantity;
            return this;
        }

        public ProductInstanceBuilder withFeature(ProductFeatureInstance feature) {
            this.features.add(feature);
            return this;
        }

        public ProductInstanceBuilder withFeature(ProductFeatureType featureType, Object value) {
            this.features.add(new ProductFeatureInstance(featureType, value));
            return this;
        }

        public ProductInstance build() {
            ProductFeatureInstances featureInstances = new ProductFeatureInstances(features);
            return new ProductInstance(id, productType, serialNumber, batchId, quantity, featureInstances);
        }
    }

    /**
     * Specialized builder for PackageInstance.
     * Has access to common fields from outer InstanceBuilder.
     */
    public class PackageInstanceBuilder {
        private final PackageType packageType;
        private List<SelectedInstance> selection;

        PackageInstanceBuilder(PackageType packageType) {
            this.packageType = packageType;
        }

        public PackageInstanceBuilder withSelection(List<SelectedInstance> selection) {
            this.selection = selection;
            return this;
        }

        public PackageInstance build() {
            return new PackageInstance(id, packageType, selection, serialNumber, batchId);
        }
    }
}
