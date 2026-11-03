package com.healthify.guardian.platform.emergencyalerting.domain.model.queries;

import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;

/**
 * Query to list the alerts of a Fragile Citizen that are not dismissed or resolved yet.
 *
 * @param careRecipientProfileId the Fragile Citizen whose active alerts are requested
 */
public record GetActiveAlertsByCareRecipientProfileIdQuery(CareRecipientProfileId careRecipientProfileId) {
}
