package com.softwarearchetypes.product;

import java.util.List;
import java.util.Optional;

import static com.softwarearchetypes.common.Preconditions.checkArgument;

/**
 * PackageInstance represents a specific instance of a PackageType with concrete instances delivered.
 *
 * Examples:
 * - PackageType: "Laptop Bundle" -> PackageInstance: delivered laptop SN-ABC, RAM batch-001, SSD SN-XYZ
 * - PackageType: "Banking Package" -> PackageInstance: delivered Gold card SN-123, insurance policy POL-456
 *
 * PackageInstance tracks:
 * - Which PackageType was purchased
 * - Concrete instances delivered (ProductInstance or PackageInstance)
 * - Optional serial number or batch for the package itself
 *
 * The delivered instances are validated against the PackageType's structure to ensure all rules are satisfied.
 */
class PackageInstance implements Instance {

    private final InstanceId id;
    private final PackageType packageType;
    private final List<SelectedInstance> selection;
    private final SerialNumber serialNumber;
    private final BatchId batchId;

    PackageInstance(InstanceId id,
                   PackageType packageType,
                   List<SelectedInstance> selection,
                   SerialNumber serialNumber,
                   BatchId batchId) {
        checkArgument(id != null, "InstanceId must be defined");
        checkArgument(packageType != null, "PackageType must be defined");
        checkArgument(selection != null && !selection.isEmpty(), "Selection cannot be empty");

        validateTrackingRequirements(packageType, serialNumber, batchId);
        validateSelection(packageType, selection);

        this.id = id;
        this.packageType = packageType;
        this.selection = List.copyOf(selection);
        this.serialNumber = serialNumber;
        this.batchId = batchId;
    }

    private static void validateTrackingRequirements(PackageType packageType,
                                                     SerialNumber serialNumber,
                                                     BatchId batchId) {
        checkArgument(serialNumber != null || batchId != null,
            "PackageInstance must have either SerialNumber or BatchId (or both)");

        ProductTrackingStrategy strategy = packageType.trackingStrategy();

        if (strategy.isTrackedIndividually() && serialNumber == null) {
            throw new IllegalArgumentException(
                "PackageType requires individual tracking (strategy: " + strategy + ")"
            );
        }

        if (strategy.isTrackedByBatch() && batchId == null) {
            throw new IllegalArgumentException(
                "PackageType requires batch tracking (strategy: " + strategy + ")"
            );
        }

        if (strategy.requiresBothTrackingMethods() && (serialNumber == null || batchId == null)) {
            throw new IllegalArgumentException(
                "PackageType requires both individual and batch tracking (strategy: " + strategy + ")"
            );
        }
    }

    private static void validateSelection(PackageType packageType, List<SelectedInstance> selection) {
        // Konwertuj SelectedInstance na SelectedProduct dla walidacji reguł pakietu
        List<SelectedProduct> selectedProducts = selection.stream()
            .map(SelectedInstance::toSelectedProduct)
            .toList();

        PackageValidationResult result = packageType.validateSelection(selectedProducts);
        if (!result.isValid()) {
            throw new IllegalArgumentException(
                "Invalid package selection: " + String.join(", ", result.errors())
            );
        }
    }

    @Override
    public InstanceId id() {
        return id;
    }

    @Override
    public Product product() {
        return packageType;
    }

    PackageType packageType() {
        return packageType;
    }

    List<SelectedInstance> selection() {
        return selection;
    }

    @Override
    public Optional<SerialNumber> serialNumber() {
        return Optional.ofNullable(serialNumber);
    }

    @Override
    public Optional<BatchId> batchId() {
        return Optional.ofNullable(batchId);
    }

    @Override
    public String toString() {
        return "PackageInstance{id=%s, type=%s, serial=%s, batch=%s, selection=%d products}".formatted(
            id,
            packageType.name(),
            serialNumber != null ? serialNumber : "none",
            batchId != null ? batchId : "none",
            selection.size()
        );
    }
}
