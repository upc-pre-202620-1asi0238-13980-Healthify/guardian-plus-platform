package com.healthify.guardian.platform.profile.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Request payload to update the personal information
 * of a person under care.
 *
 * @param firstName the care recipient's first name
 * @param lastName  the care recipient's last name
 * @param birthDate the care recipient's birth date
 */
public record UpdateCareRecipientProfileResource(

        @NotBlank(message = "{care-recipient-profile.first-name.blank}")
        String firstName,

        @NotBlank(message = "{care-recipient-profile.last-name.blank}")
        String lastName,

        @NotNull(message = "{care-recipient-profile.birth-date.blank}")
        LocalDate birthDate
) {
}