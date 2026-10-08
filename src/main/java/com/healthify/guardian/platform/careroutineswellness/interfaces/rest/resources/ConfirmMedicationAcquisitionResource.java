package com.healthify.guardian.platform.careroutineswellness.interfaces.rest.resources;

import jakarta.validation.constraints.Positive;

/**
 * Request payload for confirming the acquisition of a new medication package.
 *
 * @param dosesAdded number of doses added to the remaining balance; a whole package when omitted
 */
public record ConfirmMedicationAcquisitionResource(

        @Positive(message = "{medication-stock.doses-added.invalid}")
        Integer dosesAdded
) {
}
