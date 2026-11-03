package com.healthify.guardian.platform.emergencyalerting.domain.model.queries;

import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.DateRange;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.Severity;

/**
 * Query to page through a Fragile Citizen's alert history.
 *
 * @param careRecipientProfileId the Fragile Citizen whose history is requested
 * @param dateRange              optional triggering period filter
 * @param severityFilter         optional severity filter; {@code null} means every severity
 * @param page                   zero-based page index
 * @param size                   page size
 */
public record GetAlertHistoryByCareRecipientProfileIdQuery(
        CareRecipientProfileId careRecipientProfileId,
        DateRange dateRange,
        Severity severityFilter,
        int page,
        int size) {
}
