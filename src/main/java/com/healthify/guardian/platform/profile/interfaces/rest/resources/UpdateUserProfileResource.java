package com.healthify.guardian.platform.profile.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload to update a user's personal information.
 *
 * @param firstName the user's first name
 * @param lastName  the user's last name
 */
public record UpdateUserProfileResource(
        @NotBlank(message = "{user-profile.first-name.blank}")
        String firstName,

        @NotBlank(message = "{user-profile.last-name.blank}")
        String lastName
) {
}