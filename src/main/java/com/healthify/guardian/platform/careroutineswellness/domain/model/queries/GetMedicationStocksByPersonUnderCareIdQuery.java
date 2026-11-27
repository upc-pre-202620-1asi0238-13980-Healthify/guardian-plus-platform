package com.healthify.guardian.platform.careroutineswellness.domain.model.queries;

import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;

/**
 * Query to list the stock of every medication taken by a person under care.
 *
 * @param personUnderCareId the person whose medication stocks are requested
 */
public record GetMedicationStocksByPersonUnderCareIdQuery(PersonUnderCareId personUnderCareId) {
}
