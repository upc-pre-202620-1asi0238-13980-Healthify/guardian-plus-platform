package com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

/**
 * Immutable rule describing whether, and how, a reminder repeats after each occurrence.
 *
 * <p>Every occurrence is its own {@code Reminder} aggregate, so adherence can be tracked per dose; this
 * rule only knows how to compute when the next occurrence of the series is due.</p>
 *
 * @param frequency     how often the reminder repeats; {@code ONCE} when null
 * @param daysOfWeek    days the reminder repeats on; only meaningful (and mandatory) for {@code WEEKLY}
 * @param intervalHours hours between occurrences; only meaningful (and mandatory) for {@code HOURLY}
 */
public record RecurrenceRule(RecurrenceFrequency frequency, Set<DayOfWeek> daysOfWeek, Integer intervalHours) {

    private static final String DAYS_REQUIRED_MESSAGE_KEY = "reminder.recurrence.days-of-week.required";
    private static final String INTERVAL_INVALID_MESSAGE_KEY = "reminder.recurrence.interval-hours.invalid";
    private static final int MAX_INTERVAL_HOURS = 24;

    public RecurrenceRule {
        frequency = frequency == null ? RecurrenceFrequency.ONCE : frequency;
        if (frequency == RecurrenceFrequency.WEEKLY && (daysOfWeek == null || daysOfWeek.isEmpty())) {
            throw new IllegalArgumentException(DAYS_REQUIRED_MESSAGE_KEY);
        }
        if (frequency == RecurrenceFrequency.HOURLY
                && (intervalHours == null || intervalHours < 1 || intervalHours > MAX_INTERVAL_HOURS)) {
            throw new IllegalArgumentException(INTERVAL_INVALID_MESSAGE_KEY);
        }
        daysOfWeek = frequency == RecurrenceFrequency.WEEKLY ? Set.copyOf(EnumSet.copyOf(daysOfWeek)) : Set.of();
        intervalHours = frequency == RecurrenceFrequency.HOURLY ? intervalHours : null;
    }

    /** A rule for a reminder that is issued only once. */
    public static RecurrenceRule once() {
        return new RecurrenceRule(RecurrenceFrequency.ONCE, null, null);
    }

    /** A rule for a reminder repeated every given number of hours. */
    public static RecurrenceRule everyHours(int intervalHours) {
        return new RecurrenceRule(RecurrenceFrequency.HOURLY, null, intervalHours);
    }

    /** True when the reminder has further occurrences after the current one. */
    public boolean isRecurring() {
        return frequency != RecurrenceFrequency.ONCE;
    }

    /**
     * Computes when the next occurrence after {@code previous} is due. Occurrences that would already be in
     * the past at {@code notBefore} are skipped, so a series never floods the person with overdue reminders
     * after the platform has been down for a while.
     *
     * @param previous  scheduled time of the current occurrence
     * @param notBefore the earliest acceptable time for the next occurrence (usually now)
     * @param zone      zone the local time of day is kept in for daily and weekly rules
     * @return the next scheduled time, or empty for a one-off reminder
     */
    public Optional<Instant> nextOccurrenceAfter(Instant previous, Instant notBefore, ZoneId zone) {
        if (!isRecurring()) {
            return Optional.empty();
        }
        var next = advance(previous, zone);
        while (!next.isAfter(notBefore)) {
            next = advance(next, zone);
        }
        return Optional.of(next);
    }

    private Instant advance(Instant from, ZoneId zone) {
        return switch (frequency) {
            case HOURLY -> from.plus(Duration.ofHours(intervalHours));
            case DAILY -> from.atZone(zone).plusDays(1).toInstant();
            case WEEKLY -> {
                var next = from.atZone(zone).plusDays(1);
                while (!daysOfWeek.contains(next.getDayOfWeek())) {
                    next = next.plusDays(1);
                }
                yield next.toInstant();
            }
            case ONCE -> throw new IllegalStateException("reminder.recurrence.not-recurring");
        };
    }
}
