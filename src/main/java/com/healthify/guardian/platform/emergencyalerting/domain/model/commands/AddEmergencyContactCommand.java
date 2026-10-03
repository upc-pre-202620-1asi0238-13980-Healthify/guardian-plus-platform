package com.healthify.guardian.platform.emergencyalerting.domain.model.commands;

import java.util.UUID;

/**
 * Command to register a Care Circle member as an emergency contact of a Fragile Citizen.
 *
 * @param careRecipientProfileId the Fragile Citizen the contact belongs to
 * @param userId                 the Care Circle member registered as contact
 * @param displayName            the contact's full name
 * @param relationship           the contact's relationship with the Fragile Citizen
 * @param phoneNumber            the contact's phone number in E.164 format
 * @param priorityOrder          the contact's position in the escalation chain; 1 is the primary contact
 */
public record AddEmergencyContactCommand(
        UUID careRecipientProfileId,
        UUID userId,
        String displayName,
        String relationship,
        String phoneNumber,
        Integer priorityOrder) {
}
