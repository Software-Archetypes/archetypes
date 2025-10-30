package com.softwarearchetypes.product;

/**
 * Defines how product instances are tracked and identified in the system.
 *
 * This directly addresses the fundamental question: "How do we distinguish
 * between individual items of this product type?"
 */
enum ProductTrackingStrategy {

    /**
     * UNIQUE - One-of-a-kind product.
     * Example: Hetfield's guitar, da Vinci's painting
     */
    UNIQUE,

    /**
     * INDIVIDUALLY_TRACKED - Each instance uniquely identified.
     * Examples: iPhone, mortgage contract, parcel tracking
     */
    INDIVIDUALLY_TRACKED,

    /**
     * BATCH_TRACKED - Tracked by production batch for quality control.
     * Examples: milk bottles, pharmaceuticals, food products
     */
    BATCH_TRACKED,

    /**
     * INDIVIDUALLY_AND_BATCH_TRACKED - Both individual and batch tracking.
     * Examples: TVs, smartphones (serial + batch for recalls)
     */
    INDIVIDUALLY_AND_BATCH_TRACKED,

    /**
     * IDENTICAL - Interchangeable items, may or may not create instances.
     * Examples: screws (bulk), rice bags (instance per bag), flour (bulk)
     */
    IDENTICAL;

    /**
     * Returns true if each instance must have unique individual identity.
     */
    boolean isTrackedIndividually() {
        return this == UNIQUE
            || this == INDIVIDUALLY_TRACKED
            || this == INDIVIDUALLY_AND_BATCH_TRACKED;
    }

    /**
     * Returns true if instances are tracked by production batch.
     */
    boolean isTrackedByBatch() {
        return this == BATCH_TRACKED
            || this == INDIVIDUALLY_AND_BATCH_TRACKED;
    }

    /**
     * Returns true if both individual and batch tracking is required.
     */
    boolean requiresBothTrackingMethods() {
        return this == INDIVIDUALLY_AND_BATCH_TRACKED;
    }

    /**
     * Returns true if instances are interchangeable (no unique identity needed).
     */
    boolean isInterchangeable() {
        return this == IDENTICAL;
    }
}
