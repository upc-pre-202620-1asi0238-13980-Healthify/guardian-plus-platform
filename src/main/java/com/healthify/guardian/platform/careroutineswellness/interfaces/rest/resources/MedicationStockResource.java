package com.healthify.guardian.platform.careroutineswellness.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response payload representing the current status of a medication stock.
 *
 * @param id                   the medication stock's unique identifier
 * @param personUnderCareId    the person this stock belongs to
 * @param remainingDoses       doses currently available
 * @param dailyConsumption     expected doses consumed per day
 * @param lastAcquisitionDate  when the last package acquisition was confirmed
 * @param remainingDaysOfSupply projected days of supply left at the current consumption rate
 */
public record MedicationStockResource(
        UUID id,
        UUID personUnderCareId,
        Integer remainingDoses,
        BigDecimal dailyConsumption,
        Instant lastAcquisitionDate,
        BigDecimal remainingDaysOfSupply
) {
}
