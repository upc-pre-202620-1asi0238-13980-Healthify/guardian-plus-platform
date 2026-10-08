package com.healthify.guardian.platform.careroutineswellness.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Response payload representing the current status of one medication's stock.
 *
 * @param id                     the medication stock's unique identifier
 * @param personUnderCareId      the person this stock belongs to
 * @param medicationName         commercial or generic name of the medication
 * @param dosage                 strength of each dose
 * @param remainingDoses         doses currently available
 * @param dailyConsumption       expected doses consumed per day
 * @param packageSize            doses in one package
 * @param lastAcquisitionDate    when the last package acquisition was confirmed
 * @param remainingDaysOfSupply  projected days of supply left at the current consumption rate
 * @param estimatedDepletionDate projected day the doses run out
 * @param restockRecommended     true when the remaining supply is at or below the restock threshold
 */
public record MedicationStockResource(
        UUID id,
        UUID personUnderCareId,
        String medicationName,
        String dosage,
        Integer remainingDoses,
        BigDecimal dailyConsumption,
        Integer packageSize,
        Instant lastAcquisitionDate,
        BigDecimal remainingDaysOfSupply,
        LocalDate estimatedDepletionDate,
        boolean restockRecommended
) {
}
