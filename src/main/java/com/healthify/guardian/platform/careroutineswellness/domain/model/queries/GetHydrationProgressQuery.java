package com.healthify.guardian.platform.careroutineswellness.domain.model.queries;

import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;

import java.time.LocalDate;

/**
 * Query to measure a person under care's hydration progress on a given day.
 *
 * @param personUnderCareId the person whose progress is requested
 * @param day               the day to measure, in the person's zone
 */
public record GetHydrationProgressQuery(PersonUnderCareId personUnderCareId, LocalDate day) {
}
