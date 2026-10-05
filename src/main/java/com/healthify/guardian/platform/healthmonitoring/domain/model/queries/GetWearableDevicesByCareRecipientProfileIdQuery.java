package com.healthify.guardian.platform.healthmonitoring.domain.model.queries;

import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;

/**
 * Query for the wearable devices assigned to a care recipient.
 */
public record GetWearableDevicesByCareRecipientProfileIdQuery(CareRecipientProfileId careRecipientProfileId) {
}
