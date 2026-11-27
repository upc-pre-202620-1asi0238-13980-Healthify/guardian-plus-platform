package com.healthify.guardian.platform.careroutineswellness.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request payload for registering the stock of a new medication.
 *
 * @param personUnderCareId the person the medication belongs to
 * @param medicationName    commercial or generic name, e.g. "Losartán"
 * @param dosage            strength of each dose, e.g. "50 mg"
 * @param dailyConsumption  doses taken per day, e.g. 2
 * @param packageSize       doses in one package, e.g. 30; added on each "Añadir envase"
 * @param initialDoses      doses already at hand; zero when omitted
 */
public record RegisterMedicationStockResource(

        @NotNull(message = "{reminder.person-under-care-id.blank}")
        UUID personUnderCareId,

        @NotBlank(message = "{medication-stock.medication-name.blank}")
        @Size(max = 255, message = "{reminder.text.too-long}")
        String medicationName,

        @Size(max = 255, message = "{reminder.text.too-long}")
        String dosage,

        @NotNull(message = "{medication-stock.daily-consumption.non-positive}")
        @Positive(message = "{medication-stock.daily-consumption.non-positive}")
        BigDecimal dailyConsumption,

        @NotNull(message = "{medication-stock.package-size.non-positive}")
        @Positive(message = "{medication-stock.package-size.non-positive}")
        Integer packageSize,

        @PositiveOrZero(message = "{medication-stock.doses.negative}")
        Integer initialDoses
) {
}
