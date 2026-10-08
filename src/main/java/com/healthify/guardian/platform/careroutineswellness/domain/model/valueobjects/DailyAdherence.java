package com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects;

import java.time.LocalDate;

/**
 * Immutable adherence of a person under care on a single day.
 *
 * @param date      the day measured
 * @param due       reminders that reached the person that day
 * @param confirmed of those, how many the person confirmed
 */
public record DailyAdherence(LocalDate date, int due, int confirmed) {

    /** Confirmed over due, as a whole percentage; zero when nothing was due. */
    public int percentage() {
        return AdherenceSummary.percentage(confirmed, due);
    }
}
