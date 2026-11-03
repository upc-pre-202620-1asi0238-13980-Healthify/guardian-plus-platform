package com.healthify.guardian.platform.emergencyalerting.domain.model.commands;

import java.util.UUID;

/**
 * Command to deactivate silent mode for a Fragile Citizen.
 *
 * @param careRecipientProfileId the Fragile Citizen whose silent mode is deactivated
 */
public record DeactivateSilentModeCommand(UUID careRecipientProfileId) {
}
