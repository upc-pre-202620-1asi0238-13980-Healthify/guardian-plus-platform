package com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects;

/**
 * How often a {@code Reminder} repeats once its first occurrence has been issued.
 */
public enum RecurrenceFrequency {
    /** Issued a single time, never repeated. */
    ONCE,
    /** Repeated every day at the same local time. */
    DAILY,
    /** Repeated on specific days of the week at the same local time. */
    WEEKLY,
    /** Repeated every fixed number of hours, e.g. hydration reminders. */
    HOURLY
}
