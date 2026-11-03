package com.healthify.guardian.platform.careroutineswellness.domain.model.commands;

import java.util.UUID;

/**
 * Command to evaluate whether a medication stock warrants suggesting a restock, per
 * the Restock Policy.
 *
 * @param medicationStockId the medication stock that must be evaluated
 */
public record SuggestMedicationRestockCommand(UUID medicationStockId) {
}
