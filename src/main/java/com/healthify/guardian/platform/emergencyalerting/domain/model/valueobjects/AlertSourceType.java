package com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects;

/**
 * Kind of risk signal that raised an alert.
 */
public enum AlertSourceType {
    FALL_DETECTED(Severity.CRITICAL),
    SOS_TRIGGERED(Severity.CRITICAL),
    VITAL_SIGN_ANOMALY(Severity.HIGH),
    SAFE_ZONE_VIOLATION(Severity.HIGH),
    PROLONGED_INACTIVITY(Severity.HIGH),
    REMINDER_REISSUED(Severity.MEDIUM),
    MEDICATION_RESTOCK_SUGGESTED(Severity.MEDIUM);

    private final Severity defaultSeverity;

    AlertSourceType(Severity defaultSeverity) {
        this.defaultSeverity = defaultSeverity;
    }

    /**
     * Severity every alert of this source type is raised with. The severity is a domain decision,
     * never something the signal's sender gets to choose.
     */
    public Severity defaultSeverity() {
        return defaultSeverity;
    }

    /** Tells whether the wearable reports this signal directly, rather than another bounded context. */
    public boolean isWearableReported() {
        return this == FALL_DETECTED || this == SOS_TRIGGERED;
    }
}
