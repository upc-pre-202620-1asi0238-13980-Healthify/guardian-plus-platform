package com.healthify.guardian.platform.healthmonitoring.interfaces.rest.transform;

import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSign;
import com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources.LiveVitalSignResource;
import com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources.LiveVitalSignsResource;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Assembler building the live view of a care recipient from its latest readings, each one
 * classified against the normal range of its vital sign type.
 */
public final class LiveVitalSignsResourceFromEntityAssembler {

    private LiveVitalSignsResourceFromEntityAssembler() {
    }

    /**
     * @param liveSignalWindow readings older than this are reported without live signal (US01, scenario 4)
     */
    public static LiveVitalSignsResource toResourceFromEntities(UUID careRecipientProfileId,
                                                                List<VitalSign> latestReadings,
                                                                Duration liveSignalWindow,
                                                                Instant now) {
        var vitalSigns = latestReadings.stream()
                .map(reading -> toLiveResource(reading, liveSignalWindow, now))
                .toList();
        return new LiveVitalSignsResource(careRecipientProfileId, now, vitalSigns);
    }

    private static LiveVitalSignResource toLiveResource(VitalSign reading, Duration window, Instant now) {
        var type = reading.getVitalSignType();
        return new LiveVitalSignResource(
                reading.getId().value(),
                type.code(),
                type.displayName(),
                type.unit(),
                reading.getValue().value(),
                reading.getMeasuredAt(),
                type.normalRange().minimum(),
                type.normalRange().maximum(),
                type.classify(reading.getValue()).name(),
                !reading.getMeasuredAt().isBefore(now.minus(window)));
    }
}
