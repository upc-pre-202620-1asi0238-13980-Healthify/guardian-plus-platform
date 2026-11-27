package com.healthify.guardian.platform.careroutineswellness.domain.model.events;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.MedicationStock;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.MedicationStockId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;

import java.time.Instant;

/**
 * Raised when confirmed doses are discounted from a medication stock.
 *
 * <p>Consumed internally by {@code MedicationConsumedEventHandler}, which applies the Restock Policy.</p>
 */
public record MedicationConsumedEvent(
        MedicationStockId medicationStockId,
        PersonUnderCareId personUnderCareId,
        Integer dosesConsumed,
        Integer remainingDoses,
        Instant consumedAt) {

    public static MedicationConsumedEvent from(MedicationStock stock, Integer dosesConsumed, Instant consumedAt) {
        return new MedicationConsumedEvent(
                stock.getId(), stock.getPersonUnderCareId(), dosesConsumed, stock.getRemainingDoses(), consumedAt);
    }
}
