package com.healthify.guardian.platform.careroutineswellness.interfaces.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Integration event published by {@code careRoutinesWellness} when a medication stock's
 * remaining supply drops to, or below, the configured restock threshold.
 *
 * <p>Consumed by {@code Emergency & Alerting} to raise a {@code MEDICATION_RESTOCK_SUGGESTED}
 * alert of {@code MEDIUM} severity directed at the family member.</p>
 *
 * @param personUnderCareId the person whose medication stock needs replenishing
 * @param medicationStockId  the affected medication stock
 * @param medicationName     name of the medication running low
 * @param suggestedAt        when the suggestion was raised
 */
public record MedicationRestockSuggestedIntegrationEvent(
        UUID personUnderCareId,
        UUID medicationStockId,
        String medicationName,
        Instant suggestedAt) {
}
