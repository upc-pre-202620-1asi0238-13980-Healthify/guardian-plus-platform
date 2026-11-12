package com.healthify.guardian.platform.careroutineswellness.domain.model.queries;

import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderType;

import java.time.LocalDate;

/**
 * Query to measure how many of the reminders that reached a person under care were confirmed, day by day.
 *
 * @param personUnderCareId the person whose adherence is requested
 * @param type              only reminders of this type; every type when null
 * @param from              first day of the period, inclusive
 * @param to                last day of the period, inclusive
 */
public record GetReminderAdherenceQuery(PersonUnderCareId personUnderCareId, ReminderType type, LocalDate from, LocalDate to) {
}
