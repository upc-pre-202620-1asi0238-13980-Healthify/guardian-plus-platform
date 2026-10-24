package com.healthify.guardian.platform.healthmonitoring.application.internal.eventhandlers;

import com.healthify.guardian.platform.healthmonitoring.domain.model.events.VitalSignsThresholdsEvaluatedEvent;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignType;
import com.healthify.guardian.platform.healthmonitoring.domain.repositories.VitalSignRepository;
import com.healthify.guardian.platform.healthmonitoring.interfaces.events.VitalSignAnomalyDetectedIntegrationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Implements the <b>Tolerance Rule</b> policy ("whenever 3 consecutive readings violate the threshold"):
 * when an evaluated reading deviates, it looks at the most recent readings of the same care recipient
 * and type and, once exactly {@value #REQUIRED_CONSECUTIVE_READINGS} consecutive readings (newest first)
 * are outside the normal range, publishes a {@link VitalSignAnomalyDetectedIntegrationEvent} for
 * {@code Emergency & Alerting}.
 *
 * <p>Firing only when the streak reaches the required length (not on every later reading of the
 * same streak) keeps one anomaly from flooding the Care Circle with repeated alerts.</p>
 */
@Slf4j
@Service
public class VitalSignsThresholdsEvaluatedEventHandler {

    static final int REQUIRED_CONSECUTIVE_READINGS = 3;

    private final VitalSignRepository vitalSignRepository;
    private final ApplicationEventPublisher eventPublisher;

    public VitalSignsThresholdsEvaluatedEventHandler(VitalSignRepository vitalSignRepository,
                                                     ApplicationEventPublisher eventPublisher) {
        this.vitalSignRepository = vitalSignRepository;
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    public void on(VitalSignsThresholdsEvaluatedEvent event) {
        if (!event.hasDeviation()) {
            return;
        }
        try {
            var type = event.vitalSignType();
            var recent = vitalSignRepository.findRecentByCareRecipientProfileIdAndVitalSignType(
                    event.careRecipientProfileId(), type, REQUIRED_CONSECUTIVE_READINGS + 1);
            var streak = recent.stream()
                    .takeWhile(reading -> type.classify(reading.getValue()).isOutOfRange())
                    .count();
            if (streak != REQUIRED_CONSECUTIVE_READINGS) {
                return;
            }
            var reading = vitalSignRepository.findById(event.vitalSignId()).orElse(recent.getFirst());
            eventPublisher.publishEvent(new VitalSignAnomalyDetectedIntegrationEvent(
                    event.careRecipientProfileId().value(),
                    reading.getId().value(),
                    typeReference(type),
                    type.code(),
                    thresholdReference(event.careRecipientProfileId(), type),
                    reading.getValue().value(),
                    type.normalRange().minimum(),
                    type.normalRange().maximum(),
                    event.classification().name(),
                    REQUIRED_CONSECUTIVE_READINGS,
                    event.evaluatedAt()));
        } catch (RuntimeException e) {
            log.error("Unexpected failure applying the tolerance rule to vital sign {}", event.vitalSignId().value(), e);
        }
    }

    /** Stable identifier of a vital sign type for the integration contract. */
    static UUID typeReference(VitalSignType type) {
        return UUID.nameUUIDFromBytes(("vital-sign-type:" + type.code()).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Stable identifier of the threshold a care recipient has for a type (its normal range). It is
     * the alert source reference, so one anomaly streak raises at most one active alert per type.
     */
    static UUID thresholdReference(CareRecipientProfileId careRecipientProfileId, VitalSignType type) {
        return UUID.nameUUIDFromBytes(("vital-sign-threshold:" + careRecipientProfileId.value() + ":" + type.code())
                .getBytes(StandardCharsets.UTF_8));
    }
}
