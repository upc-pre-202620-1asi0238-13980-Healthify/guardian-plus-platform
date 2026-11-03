package com.healthify.guardian.platform.profile.domain.model.commands;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Command to create a profile for a person under care.
 *
 * @param createdByUserId the IAM user creating the profile
 * @param firstName       the care recipient's first name
 * @param lastName        the care recipient's last name
 * @param birthDate       the care recipient's birth date
 * @param profileImageUrl the profile image URL, if available
 */
public record CreateCareRecipientProfileCommand(
        UUID createdByUserId,
        String firstName,
        String lastName,
        LocalDate birthDate,
        String profileImageUrl) {
}