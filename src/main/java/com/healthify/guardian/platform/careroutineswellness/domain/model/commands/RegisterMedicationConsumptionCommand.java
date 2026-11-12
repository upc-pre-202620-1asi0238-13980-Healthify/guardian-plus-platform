package com.healthify.guardian.platform.careroutineswellness.domain.model.commands;

import java.util.UUID;

/**
 * Command to discount the doses of a confirmed medication reminder from the matching medication stock.
 *
 * @param personUnderCareId the person who took the medication
 * @param medicationStockId the stock linked to the reminder, or null to find it by medication name
 * @param medicationName    the medication taken, used when no stock is linked
 * @param doses             doses taken
 */
public record RegisterMedicationConsumptionCommand(
        UUID personUnderCareId,
        UUID medicationStockId,
        String medicationName,
        Integer doses) {
}
