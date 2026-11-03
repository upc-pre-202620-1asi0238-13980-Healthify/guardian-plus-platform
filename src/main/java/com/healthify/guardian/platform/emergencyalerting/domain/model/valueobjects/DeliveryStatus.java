package com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects;

/**
 * Status of a single alert delivery, as reported by the notification provider.
 */
public enum DeliveryStatus {
    PENDING,
    SENT,
    DELIVERED,
    FAILED;

    /** Tells whether the provider will report nothing further for a delivery in this status. */
    public boolean isFinal() {
        return this == DELIVERED || this == FAILED;
    }
}
