package com.healthify.guardian.platform.healthmonitoring.domain.model.queries;

import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;

/**
 * Query for the thresholds in force for a care recipient.
 */
public record GetActiveVitalSignThresholdsByCareRecipientProfileIdQuery(CareRecipientProfileId careRecipientProfileId) {
}
