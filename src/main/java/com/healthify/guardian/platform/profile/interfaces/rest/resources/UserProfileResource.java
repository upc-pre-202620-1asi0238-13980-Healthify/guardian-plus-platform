package com.healthify.guardian.platform.profile.interfaces.rest.resources;

import java.time.Instant;
import java.util.UUID;

/**
 * Response payload representing a Guardian+ user profile.
 *
 * @param id              the profile identifier
 * @param userId          the referenced IAM user
 * @param firstName       the user's first name
 * @param lastName        the user's last name
 * @param phoneNumber     the user's phone number
 * @param profileImageUrl the profile image URL
 * @param createdAt       when the profile was created
 * @param updatedAt       when the profile was last updated
 */
public record UserProfileResource(
        UUID id,
        UUID userId,
        String firstName,
        String lastName,
        String phoneNumber,
        String profileImageUrl,
        Instant createdAt,
        Instant updatedAt
) {
}