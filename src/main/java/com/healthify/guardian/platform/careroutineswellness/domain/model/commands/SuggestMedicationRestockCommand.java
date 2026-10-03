package com.healthify.guardian.platform.careroutineswellness.domain.model.commands;

import java.util.UUID;

/**
 * Command to evaluate whether the medication stock of a person under care warrants
 * suggesting a restock, per {@code MedicationStockPolicy}.
 *
 * @param personUnderCareId the person whose medication stock must be evaluated
 */
public record SuggestMedicationRestockCommand(UUID personUnderCareId) {
}
