package com.healthify.guardian.platform.careroutineswellness.application.queryservices;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.MedicationStock;
import com.healthify.guardian.platform.careroutineswellness.domain.model.queries.GetMedicationStockStatusQuery;

import java.util.Optional;

/**
 * Application service contract for medication stock read queries.
 */
public interface MedicationStockQueryService {

    /**
     * Handles retrieval of the current medication stock status for a person under care.
     *
     * @param query person-under-care-id query
     * @return matching medication stock, if one has been created yet
     */
    Optional<MedicationStock> handle(GetMedicationStockStatusQuery query);
}
