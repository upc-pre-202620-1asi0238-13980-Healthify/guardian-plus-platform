package com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

/**
 * Request payload to reorder a Fragile Citizen's active emergency contacts.
 *
 * @param orderedEmergencyContactIds every active contact id, primary contact first
 */
public record ReorderEmergencyContactsResource(
        @NotEmpty(message = "{emergency-contact.reorder.mismatch}")
        List<UUID> orderedEmergencyContactIds
) {
}
