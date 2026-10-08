package com.healthify.guardian.platform.emergencyalerting.domain.model.queries;

import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;

/**
 * Query to retrieve a Fragile Citizen's alerting configuration.
 *
 * @param careRecipientProfileId the Fragile Citizen whose settings are requested
 */
public record GetAlertSettingsByCareRecipientProfileIdQuery(CareRecipientProfileId careRecipientProfileId) {
}
