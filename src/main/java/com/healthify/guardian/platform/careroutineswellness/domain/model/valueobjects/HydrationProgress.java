package com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Immutable snapshot of a person under care's hydration on one day: every confirmed hydration reminder
 * counts as one glass of water.
 *
 * @param day              the day measured
 * @param glassesConsumed  hydration reminders confirmed that day
 * @param nextReminderAt   when the next hydration reminder is due, or null when none is pending
 */
public record HydrationProgress(LocalDate day, int glassesConsumed, Instant nextReminderAt) {
}
