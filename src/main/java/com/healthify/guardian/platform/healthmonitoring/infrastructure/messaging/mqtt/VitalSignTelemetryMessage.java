package com.healthify.guardian.platform.healthmonitoring.infrastructure.messaging.mqtt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Wire format of a reading published by the wearable (or the IoT simulator) on {@code guardian/vitals/<deviceId>}.
 * Only the fields Health Monitoring consumes are mapped; the rest (unit, status, normal range...) is ignored
 * because the classification is recomputed by the domain.
 *
 * @param deviceId               identifier of the linked wearable device
 * @param signalType             only {@code VITAL_SIGN_READING} carries a reading
 * @param vitalSignTypeCode      code of the {@code VitalSignType}, e.g. {@code HR}
 * @param measuredAt             when the wearable took the reading, with its offset
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record VitalSignTelemetryMessage(
        String deviceId,
        String careRecipientProfileId,
        String signalType,
        String vitalSignTypeCode,
        BigDecimal value,
        OffsetDateTime measuredAt) {

    public static final String VITAL_SIGN_READING = "VITAL_SIGN_READING";

    public boolean isVitalSignReading() {
        return VITAL_SIGN_READING.equals(signalType);
    }
}
