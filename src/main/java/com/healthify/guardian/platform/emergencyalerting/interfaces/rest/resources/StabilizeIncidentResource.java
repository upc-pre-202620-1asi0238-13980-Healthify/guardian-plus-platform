package com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources;

import jakarta.validation.constraints.Size;


/**
 * Request payload to declare an incident stabilized.
 *
 * @param notes optional free-text notes
 */
public record StabilizeIncidentResource(
        @Size(max = 2000, message = "{alert.notes.too-long}")
        String notes
) {
}
