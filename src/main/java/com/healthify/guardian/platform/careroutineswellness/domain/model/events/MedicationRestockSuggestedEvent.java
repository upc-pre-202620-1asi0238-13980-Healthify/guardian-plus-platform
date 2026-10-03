package com.healthify.guardian.platform.careroutineswellness.domain.model.events;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.MedicationStock;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.MedicationStockId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;

import java.time.Instant;

/**
 * Raised when {@code MedicationStockPolicy} determines a restock must be suggested.
 *
 * <p>Republished by {@code MedicationRestockSuggestedEventHandler} as a
 * {@code MedicationRestockSuggestedIntegrationEvent} for {@code Emergency & Alerting}.</p>
 */
public record MedicationRestockSuggestedEvent(
        MedicationStockId medicationStockId,
        PersonUnderCareId personUnderCareId,
        Instant suggestedAt) {

    public static MedicationRestockSuggestedEvent from(MedicationStock stock, Instant suggestedAt) {
        return new MedicationRestockSuggestedEvent(stock.getId(), stock.getPersonUnderCareId(), suggestedAt);
    }
}
