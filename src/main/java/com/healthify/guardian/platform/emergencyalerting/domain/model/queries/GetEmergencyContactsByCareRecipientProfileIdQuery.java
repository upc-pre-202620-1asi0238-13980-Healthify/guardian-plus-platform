package com.healthify.guardian.platform.emergencyalerting.domain.model.queries;

import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;

/**
 * Query to list a Fragile Citizen's active emergency contacts ordered by priority.
 *
 * @param careRecipientProfileId the Fragile Citizen whose contacts are requested
 */
public record GetEmergencyContactsByCareRecipientProfileIdQuery(CareRecipientProfileId careRecipientProfileId) {
}
