package com.healthify.guardian.platform.healthmonitoring.domain.model.queries;

import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.DateRange;

/**
 * Query for the readings of a care recipient measured within a period.
 */
public record GetVitalSignsByCareRecipientProfileIdAndPeriodQuery(CareRecipientProfileId careRecipientProfileId, DateRange dateRange) {
}
