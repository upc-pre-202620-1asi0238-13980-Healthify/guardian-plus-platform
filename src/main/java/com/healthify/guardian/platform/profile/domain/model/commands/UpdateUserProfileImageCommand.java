package com.healthify.guardian.platform.profile.domain.model.commands;

import java.util.UUID;

/**
 * Command to update the image of a user profile.
 */
public record UpdateUserProfileImageCommand(
        UUID userProfileId,
        String profileImageUrl) {
}