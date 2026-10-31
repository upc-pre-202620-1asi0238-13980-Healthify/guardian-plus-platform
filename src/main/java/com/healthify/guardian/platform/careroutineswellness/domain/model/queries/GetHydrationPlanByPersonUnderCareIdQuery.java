package com.healthify.guardian.platform.careroutineswellness.domain.model.queries;

import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;

/**
 * Query to retrieve the hydration plan of a person under care.
 *
 * @param personUnderCareId the person whose hydration plan is requested
 */
public record GetHydrationPlanByPersonUnderCareIdQuery(PersonUnderCareId personUnderCareId) {
}
