package com.healthify.guardian.platform.careroutineswellness.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request payload for confirming the acquisition of a new medication package.
 *
 * @param dosesAdded number of doses added to the remaining balance
 */
public record ConfirmMedicationAcquisitionResource(

        @NotNull(message = "{medication-stock.doses-added.invalid}")
        @Positive(message = "{medication-stock.doses-added.invalid}")
        Integer dosesAdded
) {
}
