package com.healthify.guardian.platform.healthmonitoring.interfaces.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Integration event published by {@code Health Monitoring} once a vital sign stays outside the
 * clinical range of its threshold for the required number of consecutive readings (Tolerance Rule).
 *
 * <p>Consumed by {@code Emergency & Alerting} to raise a {@code VITAL_SIGN_ANOMALY} alert. The
 * threshold id is the source reference, so one anomaly streak raises at most one active alert.</p>
 *
 * @param careRecipientProfileId the monitored care recipient
 * @param vitalSignId            the reading that confirmed the anomaly
 * @param vitalSignTypeId        the vital sign type that left its range
 * @param vitalSignTypeCode      the catalog code of that type, e.g. {@code HR}
 * @param vitalSignThresholdId   the threshold that was transgressed
 * @param value                  the value of the confirming reading
 * @param minimumValue           lower bound of the clinical range
 * @param maximumValue           upper bound of the clinical range
 * @param classification         {@code BELOW_RANGE} or {@code ABOVE_RANGE}
 * @param consecutiveReadings    how many consecutive readings were out of range
 * @param detectedAt             when the anomaly was confirmed
 */
public record VitalSignAnomalyDetectedIntegrationEvent(
        UUID careRecipientProfileId,
        UUID vitalSignId,
        UUID vitalSignTypeId,
        String vitalSignTypeCode,
        UUID vitalSignThresholdId,
        BigDecimal value,
        BigDecimal minimumValue,
        BigDecimal maximumValue,
        String classification,
        int consecutiveReadings,
        Instant detectedAt) {
}
