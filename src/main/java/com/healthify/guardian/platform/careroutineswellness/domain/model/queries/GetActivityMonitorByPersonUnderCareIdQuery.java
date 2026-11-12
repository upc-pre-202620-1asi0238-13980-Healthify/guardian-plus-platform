package com.healthify.guardian.platform.careroutineswellness.domain.model.queries;

import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;

/**
 * Query to retrieve the current activity status of a person under care.
 *
 * @param personUnderCareId the person whose activity monitor is requested
 */
public record GetActivityMonitorByPersonUnderCareIdQuery(PersonUnderCareId personUnderCareId) {
}
