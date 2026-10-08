package com.healthify.guardian.platform.careroutineswellness.interfaces.rest.resources;

import java.time.LocalDate;
import java.util.List;

/**
 * Response payload with a person under care's adherence over a period, e.g. "22 de 24 dosis confirmadas".
 *
 * @param type       the reminder type measured, or null when every type is included
 * @param from       first day of the period
 * @param to         last day of the period
 * @param due        reminders that reached the person
 * @param confirmed  of those, how many were confirmed
 * @param percentage confirmed over due, as a whole percentage
 * @param days       breakdown per day, one entry for every day of the period
 */
public record AdherenceResource(
        String type,
        LocalDate from,
        LocalDate to,
        int due,
        int confirmed,
        int percentage,
        List<DailyAdherenceResource> days) {

    /**
     * Adherence on a single day.
     *
     * @param date       the day
     * @param due        reminders that reached the person that day
     * @param confirmed  of those, how many were confirmed
     * @param percentage confirmed over due, as a whole percentage
     */
    public record DailyAdherenceResource(LocalDate date, int due, int confirmed, int percentage) {
    }
}
