package com.healthify.guardian.platform.profile.domain.model.commands;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Command to update the personal information of a person under care.
 */
public record UpdateCareRecipientProfileCommand(
        UUID careRecipientProfileId,
        String firstName,
        String lastName,
        LocalDate birthDate) {
}