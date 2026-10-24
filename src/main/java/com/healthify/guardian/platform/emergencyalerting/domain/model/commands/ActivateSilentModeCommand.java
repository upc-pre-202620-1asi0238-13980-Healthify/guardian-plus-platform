package com.healthify.guardian.platform.emergencyalerting.domain.model.commands;

import java.util.UUID;

/**
 * Command to activate silent mode for a Fragile Citizen.
 *
 * @param careRecipientProfileId the Fragile Citizen whose silent mode is activated
 */
public record ActivateSilentModeCommand(UUID careRecipientProfileId) {
}
