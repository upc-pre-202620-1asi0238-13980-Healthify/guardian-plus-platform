package com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources;

import java.util.UUID;

/**
 * Response payload representing an emergency contact.
 *
 * @param id                     the contact identifier
 * @param careRecipientProfileId the Fragile Citizen the contact belongs to
 * @param userId                 the Care Circle member
 * @param displayName            the contact's full name
 * @param relationship           the contact's relationship with the Fragile Citizen
 * @param phoneNumber            the contact's phone number
 * @param priorityOrder          the position in the escalation chain; 1 is the primary contact
 * @param active                 whether the contact still receives alerts
 */
public record EmergencyContactResource(
        UUID id,
        UUID careRecipientProfileId,
        UUID userId,
        String displayName,
        String relationship,
        String phoneNumber,
        Integer priorityOrder,
        boolean active
) {
}
