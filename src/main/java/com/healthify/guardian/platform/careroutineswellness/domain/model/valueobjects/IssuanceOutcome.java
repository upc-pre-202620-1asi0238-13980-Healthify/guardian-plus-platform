package com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects;

/**
 * Outcome decided by {@code ReminderIssuancePolicy} and applied by {@code Reminder#issue}.
 */
public enum IssuanceOutcome {
    ISSUE,
    SUPPRESS
}
