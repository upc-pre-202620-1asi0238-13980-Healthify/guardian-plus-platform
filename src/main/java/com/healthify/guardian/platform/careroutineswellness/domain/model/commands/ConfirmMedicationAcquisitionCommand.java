package com.healthify.guardian.platform.careroutineswellness.domain.model.commands;

import java.util.UUID;

/**
 * Command to confirm the acquisition of a new medication package, replenishing the stock.
 *
 * @param personUnderCareId the person whose medication stock must be updated
 * @param dosesAdded         number of doses added to the remaining balance
 */
public record ConfirmMedicationAcquisitionCommand(UUID personUnderCareId, Integer dosesAdded) {
}
