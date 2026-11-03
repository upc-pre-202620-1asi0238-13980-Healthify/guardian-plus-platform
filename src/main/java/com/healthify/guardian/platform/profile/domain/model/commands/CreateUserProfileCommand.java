package com.healthify.guardian.platform.profile.domain.model.commands;

import java.util.UUID;

/**
 * Command to create the descriptive profile associated with an IAM user.
 *
 * @param userId          the referenced IAM user identifier
 * @param firstName       the user's first name
 * @param lastName        the user's last name
 * @param phoneNumber     the user's contact phone number
 * @param profileImageUrl the profile image URL, if available
 */
public record CreateUserProfileCommand(
        UUID userId,
        String firstName,
        String lastName,
        String phoneNumber,
        String profileImageUrl) {
}