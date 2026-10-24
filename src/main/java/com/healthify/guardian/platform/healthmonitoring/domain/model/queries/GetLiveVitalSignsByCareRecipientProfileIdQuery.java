package com.healthify.guardian.platform.healthmonitoring.domain.model.queries;

import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;

/**
 * Query for the latest emitted reading of every vital sign type of a care recipient.
 */
public record GetLiveVitalSignsByCareRecipientProfileIdQuery(CareRecipientProfileId careRecipientProfileId) {
}
