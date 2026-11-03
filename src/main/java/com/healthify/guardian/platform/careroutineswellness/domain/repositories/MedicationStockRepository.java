package com.healthify.guardian.platform.careroutineswellness.domain.repositories;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.MedicationStock;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.MedicationStockId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;

import java.util.List;
import java.util.Optional;

/**
 * Medication stock aggregate repository port.
 */
public interface MedicationStockRepository {

    /**
     * Retrieves a medication stock by its unique identifier.
     *
     * @param id the medication stock identifier
     * @return the matching medication stock, if found
     */
    Optional<MedicationStock> findById(MedicationStockId id);

    /**
     * Retrieves the stock of every medication taken by a person under care.
     *
     * @param personUnderCareId the person whose medication stocks are requested
     * @return the matching medication stocks, ordered by medication name
     */
    List<MedicationStock> findByPersonUnderCareId(PersonUnderCareId personUnderCareId);

    /**
     * Persists a medication stock (create or update) and publishes its registered domain events.
     *
     * @param stock the medication stock to save
     * @return the saved medication stock
     */
    MedicationStock save(MedicationStock stock);
}
