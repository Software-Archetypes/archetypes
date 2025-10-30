package com.softwarearchetypes.product;

/**
 * Records the status of the delivery of a particular ServiceInstance.
 *
 * ServiceInstances are executions of a process, with a lifecycle:
 * SCHEDULED → EXECUTING → COMPLETED/CANCELLED
 */
enum ServiceDeliveryStatus {

    /**
     * The ServiceInstance has been scheduled for delivery.
     */
    SCHEDULED,

    /**
     * The ServiceInstance is currently in the process of delivery.
     */
    EXECUTING,

    /**
     * The delivery of the ServiceInstance has been completed successfully.
     */
    COMPLETED,

    /**
     * The ServiceInstance has been cancelled before or during execution.
     */
    CANCELLED;

    boolean isFinished() {
        return this == COMPLETED || this == CANCELLED;
    }

    boolean isInProgress() {
        return this == EXECUTING;
    }

    boolean canStart() {
        return this == SCHEDULED;
    }

    boolean canCancel() {
        return this == SCHEDULED || this == EXECUTING;
    }
}
