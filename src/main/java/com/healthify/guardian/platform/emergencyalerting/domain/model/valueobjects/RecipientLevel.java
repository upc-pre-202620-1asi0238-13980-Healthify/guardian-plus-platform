package com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects;

/**
 * Level of the escalation chain an alert delivery was sent at.
 *
 * <p>Declared in escalation order, so {@link #compareTo} reflects how far an alert has escalated.</p>
 */
public enum RecipientLevel {
    PRIMARY,
    SECONDARY,
    BROADCAST;

    /**
     * Returns the level that follows this one in the escalation chain.
     *
     * @return the next level, or {@code BROADCAST} itself once the chain is exhausted
     */
    public RecipientLevel next() {
        return switch (this) {
            case PRIMARY -> SECONDARY;
            case SECONDARY, BROADCAST -> BROADCAST;
        };
    }
}
