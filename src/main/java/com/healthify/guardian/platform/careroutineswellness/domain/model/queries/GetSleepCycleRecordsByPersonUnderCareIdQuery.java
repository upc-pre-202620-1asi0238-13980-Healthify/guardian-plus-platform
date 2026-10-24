package com.healthify.guardian.platform.careroutineswellness.domain.model.queries;

import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;

import java.time.Instant;

/**
 * Query to list the closed sleep cycles of a person under care, most recent first, optionally narrowed to
 * the cycles that ended within a period.
 *
 * @param personUnderCareId the person whose sleep cycles are requested
 * @param from              earliest end time, inclusive; unbounded when null
 * @param to                latest end time, exclusive; unbounded when null
 */
public record GetSleepCycleRecordsByPersonUnderCareIdQuery(PersonUnderCareId personUnderCareId, Instant from, Instant to) {
}
