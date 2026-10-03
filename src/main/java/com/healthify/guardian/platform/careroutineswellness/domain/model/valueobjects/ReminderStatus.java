package com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects;

/**
 * Lifecycle status of a {@code Reminder} aggregate.
 */
public enum ReminderStatus {
    SCHEDULED,
    ISSUED,
    CONFIRMED,
    CANCELLED,
    REISSUED,
    SUPPRESSED
}
