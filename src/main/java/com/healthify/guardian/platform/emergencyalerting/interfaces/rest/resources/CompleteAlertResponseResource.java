package com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources;

import jakarta.validation.constraints.Size;


/**
 * Request payload to record the outcome of an intervention.
 *
 * @param notes optional free-text outcome
 */
public record CompleteAlertResponseResource(
        @Size(max = 2000, message = "{alert.notes.too-long}")
        String notes
) {
}
