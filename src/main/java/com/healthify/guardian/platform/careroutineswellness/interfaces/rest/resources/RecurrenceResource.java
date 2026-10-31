package com.healthify.guardian.platform.careroutineswellness.interfaces.rest.resources;

import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.RecurrenceFrequency;

import java.time.DayOfWeek;
import java.util.Set;

/**
 * How a reminder repeats, both in requests and responses.
 *
 * @param frequency     {@code ONCE} ("Una sola vez"), {@code DAILY} ("Todos los días"), {@code WEEKLY}
 *                      ("Días específicos") or {@code HOURLY} (e.g. "Cada 2 horas")
 * @param daysOfWeek    days a {@code WEEKLY} reminder repeats on, e.g. {@code ["MONDAY","THURSDAY"]}
 * @param intervalHours hours between occurrences of an {@code HOURLY} reminder
 */
public record RecurrenceResource(RecurrenceFrequency frequency, Set<DayOfWeek> daysOfWeek, Integer intervalHours) {
}
