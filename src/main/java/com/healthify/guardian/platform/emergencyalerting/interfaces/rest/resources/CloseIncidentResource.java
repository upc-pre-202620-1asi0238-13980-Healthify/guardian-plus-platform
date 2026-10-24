package com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources;

import jakarta.validation.constraints.Size;


/**
 * Request payload to close an incident.
 *
 * @param notes optional free-text notes
 */
public record CloseIncidentResource(
        @Size(max = 2000, message = "{alert.notes.too-long}")
        String notes
) {
}
