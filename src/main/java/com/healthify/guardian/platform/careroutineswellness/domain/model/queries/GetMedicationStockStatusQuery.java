package com.healthify.guardian.platform.careroutineswellness.domain.model.queries;

import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;

/**
 * Query to retrieve the current medication stock status of a person under care.
 *
 * @param personUnderCareId the person whose medication stock is requested
 */
public record GetMedicationStockStatusQuery(PersonUnderCareId personUnderCareId) {
}
