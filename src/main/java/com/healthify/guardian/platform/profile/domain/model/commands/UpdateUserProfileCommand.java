package com.healthify.guardian.platform.profile.domain.model.commands;

import java.util.UUID;

/**
 * Command to update the personal information of a user profile.
 *
 * @param userProfileId the profile to update
 * @param firstName     the updated first name
 * @param lastName      the updated last name
 */
public record UpdateUserProfileCommand(
        UUID userProfileId,
        String firstName,
        String lastName) {
}