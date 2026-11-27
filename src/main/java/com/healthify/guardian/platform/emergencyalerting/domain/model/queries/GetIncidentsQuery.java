package com.healthify.guardian.platform.emergencyalerting.domain.model.queries;

import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertSourceType;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.DateRange;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.Severity;

/**
 * Query to page through incidents, optionally filtered by the monitored person,
 * event type, clinical severity and the triggering period of the alert that
 * originated the incident.
 *
 * @param patientId optional monitored person identifier; null means every
 *                  patient
 * @param dateRange optional alert triggering period
 * @param eventType optional event type filter
 * @param severity  optional clinical severity filter
 * @param page      zero-based page index
 * @param size      page size
 */
public record GetIncidentsQuery(
        CareRecipientProfileId patientId,
        DateRange dateRange,
        AlertSourceType eventType,
        Severity severity,
        int page,
        int size) {
}