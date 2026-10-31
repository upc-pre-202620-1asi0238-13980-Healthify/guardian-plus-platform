package com.healthify.guardian.platform.healthmonitoring.domain.model.queries;

import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;

/**
 * Query for every health report of a care recipient.
 */
public record GetAllHealthReportsByCareRecipientProfileIdQuery(CareRecipientProfileId careRecipientProfileId) {
}
