package com.healthify.guardian.platform.careroutineswellness.domain.model.queries;

import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;

/**
 * Query to list the most recent activity log entries of a person under care.
 *
 * @param personUnderCareId the person whose activity log is requested
 * @param limit             maximum number of entries to return
 */
public record GetActivityLogEntriesByPersonUnderCareIdQuery(PersonUnderCareId personUnderCareId, int limit) {
}
