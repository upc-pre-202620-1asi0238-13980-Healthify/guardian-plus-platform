package com.healthify.guardian.platform.emergencyalerting.domain.model.commands;

import java.util.UUID;

/**
 * Command to remove an emergency contact from future alert deliveries.
 *
 * @param emergencyContactId the contact to deactivate
 */
public record DeactivateEmergencyContactCommand(UUID emergencyContactId) {
}
