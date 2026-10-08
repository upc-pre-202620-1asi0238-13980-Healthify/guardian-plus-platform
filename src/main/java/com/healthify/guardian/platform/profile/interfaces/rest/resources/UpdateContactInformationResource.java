package com.healthify.guardian.platform.profile.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload to update a user's contact information.
 *
 * @param phoneNumber the new phone number
 */
public record UpdateContactInformationResource(
        @NotBlank(message = "{user-profile.phone-number.blank}")
        String phoneNumber
) {
}