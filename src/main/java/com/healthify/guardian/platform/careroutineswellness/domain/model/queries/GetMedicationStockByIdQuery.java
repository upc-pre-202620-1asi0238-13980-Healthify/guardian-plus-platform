package com.healthify.guardian.platform.careroutineswellness.domain.model.queries;

import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.MedicationStockId;

/**
 * Query to retrieve the current status of a single medication stock.
 *
 * @param medicationStockId the medication stock identifier
 */
public record GetMedicationStockByIdQuery(MedicationStockId medicationStockId) {
}
