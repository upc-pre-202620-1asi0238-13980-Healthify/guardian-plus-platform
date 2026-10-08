package com.healthify.guardian.platform.careroutineswellness.application.queryservices;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.MedicationStock;
import com.healthify.guardian.platform.careroutineswellness.domain.model.queries.GetMedicationStockByIdQuery;
import com.healthify.guardian.platform.careroutineswellness.domain.model.queries.GetMedicationStocksByPersonUnderCareIdQuery;

import java.util.List;
import java.util.Optional;

/**
 * Application service contract for medication stock read queries.
 */
public interface MedicationStockQueryService {

    /**
     * Handles retrieval of a single medication stock.
     *
     * @param query medication-stock-id query
     * @return matching medication stock, if found
     */
    Optional<MedicationStock> handle(GetMedicationStockByIdQuery query);

    /**
     * Handles retrieval of the stock of every medication taken by a person under care.
     *
     * @param query person-under-care-id query
     * @return matching medication stocks, ordered by medication name
     */
    List<MedicationStock> handle(GetMedicationStocksByPersonUnderCareIdQuery query);
}
