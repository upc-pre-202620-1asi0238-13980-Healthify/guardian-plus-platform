package com.healthify.guardian.platform.careroutineswellness.domain.model.events;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.MedicationStock;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.MedicationStockId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;

import java.time.Instant;

/**
 * Raised after a medication acquisition has been confirmed and the stock balance updated.
 */
public record MedicationStockUpdatedEvent(
        MedicationStockId medicationStockId,
        PersonUnderCareId personUnderCareId,
        Integer remainingDoses,
        Instant updatedAt) {

    public static MedicationStockUpdatedEvent from(MedicationStock stock, Instant updatedAt) {
        return new MedicationStockUpdatedEvent(
                stock.getId(), stock.getPersonUnderCareId(), stock.getRemainingDoses(), updatedAt);
    }
}
