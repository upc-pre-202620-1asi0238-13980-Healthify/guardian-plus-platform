package com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects;

/**
 * Severity of an alert, which governs how it is dispatched to the Care Circle.
 */
public enum Severity {
    CRITICAL,
    HIGH,
    MEDIUM;

    /**
     * Tells whether this severity may skip the escalation chain and reach every active contact at
     * once, provided the Fragile Citizen's {@code AlertSettings} ask for it.
     */
    public boolean allowsImmediateBroadcast() {
        return this == CRITICAL;
    }

    /** Tells whether an unacknowledged alert of this severity may escalate past the primary contact. */
    public boolean allowsEscalation() {
        return this == CRITICAL || this == HIGH;
    }

    /** Tells whether an alert of this severity must be audible even while silent mode is active. */
    public boolean overridesSilentMode() {
        return this == CRITICAL;
    }

    /** Tells whether every delivery of an alert of this severity must be backed up by SMS. */
    public boolean requiresSmsBackup() {
        return this == CRITICAL;
    }
}
