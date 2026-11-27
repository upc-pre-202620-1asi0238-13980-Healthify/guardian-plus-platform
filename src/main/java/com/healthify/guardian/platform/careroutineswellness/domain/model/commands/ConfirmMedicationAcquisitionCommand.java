package com.healthify.guardian.platform.careroutineswellness.domain.model.commands;

import java.util.UUID;

/**
 * Command to confirm the acquisition of a new medication package, replenishing the stock.
 *
 * @param medicationStockId the medication stock to replenish
 * @param dosesAdded        number of doses added to the remaining balance; a whole package when null
 */
public record ConfirmMedicationAcquisitionCommand(UUID medicationStockId, Integer dosesAdded) {
}
