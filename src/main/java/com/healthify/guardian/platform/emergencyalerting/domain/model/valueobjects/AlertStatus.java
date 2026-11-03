package com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects;

import java.util.Map;
import java.util.Set;

/**
 * Lifecycle status of an {@code Alert} aggregate.
 *
 * <p>{@code PENDING_CONFIRMATION → TRIGGERED → ESCALATED → ACKNOWLEDGED → RESOLVED}, with
 * {@code DISMISSED} as the exit for a false positive.</p>
 */
public enum AlertStatus {
    PENDING_CONFIRMATION,
    TRIGGERED,
    ESCALATED,
    ACKNOWLEDGED,
    DISMISSED,
    RESOLVED;

    private static final Map<AlertStatus, Set<AlertStatus>> ALLOWED_TRANSITIONS = Map.of(
            PENDING_CONFIRMATION, Set.of(TRIGGERED, DISMISSED),
            TRIGGERED, Set.of(ESCALATED, ACKNOWLEDGED),
            ESCALATED, Set.of(ACKNOWLEDGED),
            ACKNOWLEDGED, Set.of(RESOLVED),
            DISMISSED, Set.of(),
            RESOLVED, Set.of());

    /**
     * Tells whether an alert in this status may move to the given one.
     *
     * @param target the status to move to
     * @return true if the transition is part of the alert lifecycle
     */
    public boolean canTransitionTo(AlertStatus target) {
        return ALLOWED_TRANSITIONS.get(this).contains(target);
    }

    /** Tells whether this status is final, i.e. the alert no longer requires attention. */
    public boolean isTerminal() {
        return this == DISMISSED || this == RESOLVED;
    }

    /** Tells whether an alert in this status has been dispatched and still awaits a recipient's acknowledgement. */
    public boolean isAwaitingAcknowledgement() {
        return this == TRIGGERED || this == ESCALATED;
    }
}
