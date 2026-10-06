package com.healthify.guardian.platform.profile.domain.model.commands;

import java.util.UUID;

/**
 * Command to update the image of a person under care profile.
 */
public record UpdateCareRecipientProfileImageCommand(
        UUID careRecipientProfileId,
        String profileImageUrl) {
}