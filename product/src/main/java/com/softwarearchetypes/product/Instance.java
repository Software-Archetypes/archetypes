package com.softwarearchetypes.product;

import java.util.Optional;

/**
 * Instance represents a specific instance/exemplar of a Product (either ProductType or PackageType).
 * While Product defines WHAT can be sold, Instance represents WHAT WAS actually sold/created.
 * <p>
 * Common interface for:
 * - ProductInstance: specific instance of a ProductType (e.g., iPhone with serial ABC123)
 * - PackageInstance: specific instance of a PackageType (e.g., laptop bundle with customer's choices)
 * <p>
 * Every instance must be tracked by at least one of:
 * - SerialNumber (individual tracking)
 * - Batch (group tracking for quality control)
 * The tracking requirements are defined by the Product's tracking strategy.
 */
interface Instance {

    InstanceId id();

    Product product();

    Optional<SerialNumber> serialNumber();

    Optional<BatchId> batchId();

    default InstanceBuilder builder(InstanceId id) {
        return new InstanceBuilder(id);
    }
}
