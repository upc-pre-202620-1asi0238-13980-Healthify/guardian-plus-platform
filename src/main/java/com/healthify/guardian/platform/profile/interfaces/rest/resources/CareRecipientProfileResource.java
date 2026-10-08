package com.healthify.guardian.platform.profile.interfaces.rest.resources;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Response payload representing a person under care profile.
 *
 * @param id              the care recipient profile identifier
 * @param createdByUserId the Guardian+ user who created the profile
 * @param firstName       the care recipient's first name
 * @param lastName        the care recipient's last name
 * @param birthDate       the care recipient's birth date
 * @param profileImageUrl the profile image URL
 * @param createdAt       when the profile was created
 * @param updatedAt       when the profile was last updated
 */
public record CareRecipientProfileResource(
        UUID id,
        UUID createdByUserId,
        String firstName,
        String lastName,
        LocalDate birthDate,
        String profileImageUrl,
        Instant createdAt,
        Instant updatedAt
) {
}