package com.healthify.guardian.platform.healthmonitoring.interfaces.rest.transform;

import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSign;
import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSignThreshold;
import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSignType;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignTypeId;
import com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources.LiveVitalSignResource;
import com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources.LiveVitalSignsResource;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Assembler building the live view of a care recipient from its latest readings, the type catalog
 * and the thresholds in force.
 */
public final class LiveVitalSignsResourceFromEntityAssembler {

    private LiveVitalSignsResourceFromEntityAssembler() {
    }

    /**
     * @param liveSignalWindow readings older than this are reported without live signal (US01, scenario 4)
     */
    public static LiveVitalSignsResource toResourceFromEntities(UUID careRecipientProfileId,
                                                                List<VitalSign> latestReadings,
                                                                List<VitalSignType> types,
                                                                List<VitalSignThreshold> activeThresholds,
                                                                Duration liveSignalWindow,
                                                                Instant now) {
        var typesById = types.stream().collect(Collectors.toMap(VitalSignType::getId, Function.identity()));
        var thresholdsByType = activeThresholds.stream()
                .collect(Collectors.toMap(VitalSignThreshold::getVitalSignTypeId, Function.identity(), (a, b) -> a));
        var vitalSigns = latestReadings.stream()
                .map(reading -> toLiveResource(reading, typesById.get(reading.getVitalSignTypeId()),
                        thresholdsByType.get(reading.getVitalSignTypeId()), liveSignalWindow, now))
                .toList();
        return new LiveVitalSignsResource(careRecipientProfileId, now, vitalSigns);
    }

    private static LiveVitalSignResource toLiveResource(VitalSign reading, VitalSignType type,
                                                        VitalSignThreshold threshold, Duration window, Instant now) {
        VitalSignTypeId typeId = reading.getVitalSignTypeId();
        return new LiveVitalSignResource(
                reading.getId().value(),
                typeId.value(),
                type == null ? null : type.getCode().value(),
                type == null ? null : type.getName(),
                type == null ? null : type.getUnit(),
                reading.getValue().value(),
                reading.getMeasuredAt(),
                threshold == null ? null : threshold.classify(reading.getValue()).name(),
                !reading.getMeasuredAt().isBefore(now.minus(window)));
    }
}
