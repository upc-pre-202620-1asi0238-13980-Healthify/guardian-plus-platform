package com.healthify.guardian.platform.careroutineswellness.domain.model.commands;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Command to register the stock of a new medication taken by a person under care.
 *
 * @param personUnderCareId the person the medication belongs to
 * @param medicationName    commercial or generic name of the medication
 * @param dosage            strength of each dose, e.g. "50 mg"
 * @param dailyConsumption  doses taken per day, used to project the remaining supply
 * @param packageSize       doses contained in one package, added on each acquisition by default
 * @param initialDoses      doses already at hand; zero when null
 */
public record RegisterMedicationStockCommand(
        UUID personUnderCareId,
        String medicationName,
        String dosage,
        BigDecimal dailyConsumption,
        Integer packageSize,
        Integer initialDoses) {
}
