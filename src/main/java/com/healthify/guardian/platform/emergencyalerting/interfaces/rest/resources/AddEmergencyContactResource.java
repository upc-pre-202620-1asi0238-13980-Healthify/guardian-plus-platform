package com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Request payload to register a Care Circle member as an emergency contact.
 *
 * @param careRecipientProfileId the Fragile Citizen the contact belongs to
 * @param userId                 the Care Circle member
 * @param displayName            the contact's full name
 * @param relationship           the contact's relationship with the Fragile Citizen
 * @param phoneNumber            phone number in E.164 format, e.g. {@code +51987654321}
 * @param priorityOrder          optional position in the escalation chain; last if omitted
 */
public record AddEmergencyContactResource(
        @NotNull(message = "{alert.care-recipient-profile-id.blank}")
        UUID careRecipientProfileId,

        @NotNull(message = "{alert.user-id.blank}")
        UUID userId,

        @NotBlank(message = "{emergency-contact.display-name.invalid}")
        @Size(max = 100, message = "{emergency-contact.display-name.invalid}")
        String displayName,

        @NotBlank(message = "{emergency-contact.relationship.invalid}")
        @Size(max = 50, message = "{emergency-contact.relationship.invalid}")
        String relationship,

        @NotBlank(message = "{phone-number.invalid}")
        @Pattern(regexp = "^\\+[1-9]\\d{7,14}$", message = "{phone-number.invalid}")
        String phoneNumber,

        @Min(value = 1, message = "{priority-order.invalid}")
        Integer priorityOrder
) {
}
