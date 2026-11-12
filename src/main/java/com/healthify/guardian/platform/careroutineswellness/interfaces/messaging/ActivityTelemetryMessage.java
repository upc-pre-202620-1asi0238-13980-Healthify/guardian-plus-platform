package com.healthify.guardian.platform.careroutineswellness.interfaces.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Wire format of the activity telemetry published by the wearable (or the IoT simulator) on
 * {@code guardian/activity/<deviceId>}.
 *
 * <p>This is the external, hardware-facing contract; {@link ActivityTelemetryConsumer} is the
 * Anti-Corruption Layer that translates it into this bounded context's own commands. Only the fields
 * this context consumes are mapped; the rest (severity, threshold, intensity...) is ignored because the
 * inactivity rule is applied by the domain with the person's own threshold.</p>
 *
 * @param deviceId               identifier of the wearable device, only used for logging
 * @param careRecipientProfileId the person wearing the device
 * @param signalType             {@code ACTIVITY_SAMPLE}, {@code ACTIVITY_RESUMED} or {@code PROLONGED_INACTIVITY}
 * @param measuredAt             when the wearable took the sample, with its offset
 * @param steps                  steps counted since the previous sample
 * @param inactiveMinutes        minutes without movement accumulated by the wearable
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ActivityTelemetryMessage(
        String deviceId,
        String careRecipientProfileId,
        String signalType,
        OffsetDateTime measuredAt,
        Integer steps,
        BigDecimal inactiveMinutes) {

    public static final String ACTIVITY_SAMPLE = "ACTIVITY_SAMPLE";

    /**
     * Only periodic samples are consumed: they carry everything the domain needs, while the wearable's own
     * {@code ACTIVITY_RESUMED} and {@code PROLONGED_INACTIVITY} verdicts are derived from the same counter
     * and are emitted in the same cycle, so consuming them too would count that cycle twice.
     */
    public boolean isActivitySample() {
        return ACTIVITY_SAMPLE.equals(signalType);
    }
}
