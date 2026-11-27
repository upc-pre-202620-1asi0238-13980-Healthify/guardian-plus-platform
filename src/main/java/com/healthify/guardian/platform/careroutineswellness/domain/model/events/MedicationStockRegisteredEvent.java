package com.healthify.guardian.platform.careroutineswellness.domain.model.events;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.MedicationStock;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.MedicationStockId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;

import java.time.Instant;

/**
 * Raised when the stock of a new medication is registered for a person under care.
 */
public record MedicationStockRegisteredEvent(
        MedicationStockId medicationStockId,
        PersonUnderCareId personUnderCareId,
        String medicationName,
        Integer remainingDoses,
        Instant registeredAt) {

    public static MedicationStockRegisteredEvent from(MedicationStock stock) {
        return new MedicationStockRegisteredEvent(
                stock.getId(), stock.getPersonUnderCareId(), stock.getMedication().name(),
                stock.getRemainingDoses(), stock.getLastAcquisitionDate());
    }
}
