package com.healthify.guardian.platform.careroutineswellness.domain.repositories;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.MedicationStock;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;

import java.util.Optional;

/**
 * Medication stock aggregate repository port.
 */
public interface MedicationStockRepository {

    /**
     * Retrieves the single medication stock for a person under care.
     *
     * @param personUnderCareId the person whose medication stock is requested
     * @return the matching medication stock, if one has been created yet
     */
    Optional<MedicationStock> findByPersonUnderCareId(PersonUnderCareId personUnderCareId);

    /**
     * Persists a medication stock (create or update) and publishes its registered domain events.
     *
     * @param stock the medication stock to save
     * @return the saved medication stock
     */
    MedicationStock save(MedicationStock stock);
}
