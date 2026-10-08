package com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.Reminder;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Immutable adherence of a person under care over a period: how many of the reminders that reached them
 * were confirmed, overall and day by day.
 *
 * @param type      the reminder type measured, or null when every type is included
 * @param from      first day of the period, inclusive
 * @param to        last day of the period, inclusive
 * @param due       reminders that reached the person during the period
 * @param confirmed of those, how many the person confirmed
 * @param days      breakdown per day, one entry for every day of the period
 */
public record AdherenceSummary(
        ReminderType type, LocalDate from, LocalDate to, int due, int confirmed, List<DailyAdherence> days) {

    public AdherenceSummary {
        days = List.copyOf(days);
    }

    /**
     * Summarizes adherence over a period. Only reminders that reached the person count
     * ({@link Reminder#hasReachedPerson()}); each one is attributed to the local day of its scheduled time.
     *
     * @param reminders the person's reminders; those outside the period or of another type are ignored
     * @param type      the reminder type to measure, or null for every type
     * @param from      first day of the period, inclusive
     * @param to        last day of the period, inclusive
     * @param zone      the zone days are counted in
     * @return the adherence summary, with one entry per day of the period
     */
    public static AdherenceSummary from(
            Collection<Reminder> reminders, ReminderType type, LocalDate from, LocalDate to, ZoneId zone) {
        var days = new ArrayList<DailyAdherence>();
        var totalDue = 0;
        var totalConfirmed = 0;
        for (var day = from; !day.isAfter(to); day = day.plusDays(1)) {
            var current = day;
            var dueThatDay = reminders.stream()
                    .filter(reminder -> type == null || reminder.getType() == type)
                    .filter(Reminder::hasReachedPerson)
                    .filter(reminder -> reminder.getScheduledTime().atZone(zone).toLocalDate().equals(current))
                    .toList();
            var confirmed = (int) dueThatDay.stream()
                    .filter(reminder -> reminder.getStatus() == ReminderStatus.CONFIRMED)
                    .count();
            days.add(new DailyAdherence(day, dueThatDay.size(), confirmed));
            totalDue += dueThatDay.size();
            totalConfirmed += confirmed;
        }
        return new AdherenceSummary(type, from, to, totalDue, totalConfirmed, days);
    }

    /** Confirmed over due, as a whole percentage; zero when nothing was due. */
    public int percentage() {
        return percentage(confirmed, due);
    }

    static int percentage(int confirmed, int due) {
        return due == 0 ? 0 : (int) Math.round(confirmed * 100.0 / due);
    }
}
