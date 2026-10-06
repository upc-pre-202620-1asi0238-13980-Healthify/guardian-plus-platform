package com.healthify.guardian.platform.profile.domain.model.commands;

import java.util.UUID;

/**
 * Command to update the contact information of a user profile.
 *
 * @param userProfileId the profile to update
 * @param phoneNumber   the updated phone number
 */
public record UpdateContactInformationCommand(
        UUID userProfileId,
        String phoneNumber) {
}