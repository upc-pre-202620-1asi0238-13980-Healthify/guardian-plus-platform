package com.healthify.guardian.platform.profile.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request payload to create a user profile.
 *
 * @param userId          the referenced IAM user
 * @param firstName       the user's first name
 * @param lastName        the user's last name
 * @param phoneNumber     the user's phone number
 * @param profileImageUrl optional profile image URL
 */
public record CreateUserProfileResource(
        @NotNull(message = "{user-profile.user-id.blank}")
        UUID userId,

        @NotBlank(message = "{user-profile.first-name.blank}")
        String firstName,

        @NotBlank(message = "{user-profile.last-name.blank}")
        String lastName,

        @NotBlank(message = "{user-profile.phone-number.blank}")
        String phoneNumber,

        String profileImageUrl
) {
}