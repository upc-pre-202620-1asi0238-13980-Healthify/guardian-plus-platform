package com.healthify.guardian.platform.profile.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Request payload to create a profile for a person under care.
 *
 * @param createdByUserId the Guardian+ user who creates the profile
 * @param firstName       the care recipient's first name
 * @param lastName        the care recipient's last name
 * @param birthDate       the care recipient's birth date
 * @param profileImageUrl optional profile image URL
 */
public record CreateCareRecipientProfileResource(
        @NotNull(message = "{care-recipient-profile.created-by-user-id.blank}")
        UUID createdByUserId,

        @NotBlank(message = "{care-recipient-profile.first-name.blank}")
        String firstName,

        @NotBlank(message = "{care-recipient-profile.last-name.blank}")
        String lastName,

        @NotNull(message = "{care-recipient-profile.birth-date.blank}")
        LocalDate birthDate,

        String profileImageUrl
) {
}