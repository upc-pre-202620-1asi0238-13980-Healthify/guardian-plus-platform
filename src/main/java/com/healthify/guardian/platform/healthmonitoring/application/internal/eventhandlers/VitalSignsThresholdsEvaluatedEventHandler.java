package com.healthify.guardian.platform.healthmonitoring.application.internal.eventhandlers;

import com.healthify.guardian.platform.healthmonitoring.domain.model.events.VitalSignsThresholdsEvaluatedEvent;
import com.healthify.guardian.platform.healthmonitoring.domain.repositories.VitalSignRepository;
import com.healthify.guardian.platform.healthmonitoring.domain.repositories.VitalSignThresholdRepository;
import com.healthify.guardian.platform.healthmonitoring.domain.repositories.VitalSignTypeRepository;
import com.healthify.guardian.platform.healthmonitoring.interfaces.events.VitalSignAnomalyDetectedIntegrationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Implements the <b>Tolerance Rule</b> policy: when an evaluated reading deviates, it looks at the
 * most recent readings of the same care recipient and type and, once exactly
 * {@code requiredConsecutiveHits} consecutive readings (newest first) are out of range, publishes a
 * {@link VitalSignAnomalyDetectedIntegrationEvent} for {@code Emergency & Alerting}.
 *
 * <p>Firing only when the streak reaches the required length (not on every later reading of the
 * same streak) keeps one anomaly from flooding the Care Circle with repeated alerts.</p>
 */
@Slf4j
@Service
public class VitalSignsThresholdsEvaluatedEventHandler {

    private final VitalSignRepository vitalSignRepository;
    private final VitalSignThresholdRepository vitalSignThresholdRepository;
    private final VitalSignTypeRepository vitalSignTypeRepository;
    private final ApplicationEventPublisher eventPublisher;

    public VitalSignsThresholdsEvaluatedEventHandler(VitalSignRepository vitalSignRepository,
                                                     VitalSignThresholdRepository vitalSignThresholdRepository,
                                                     VitalSignTypeRepository vitalSignTypeRepository,
                                                     ApplicationEventPublisher eventPublisher) {
        this.vitalSignRepository = vitalSignRepository;
        this.vitalSignThresholdRepository = vitalSignThresholdRepository;
        this.vitalSignTypeRepository = vitalSignTypeRepository;
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    public void on(VitalSignsThresholdsEvaluatedEvent event) {
        if (!event.hasDeviation()) {
            return;
        }
        try {
            var threshold = vitalSignThresholdRepository.findById(event.vitalSignThresholdId());
            if (threshold.isEmpty() || !threshold.get().isActive()) {
                return;
            }
            int requiredHits = threshold.get().getRequiredConsecutiveHits();
            var recent = vitalSignRepository.findRecentByCareRecipientProfileIdAndVitalSignTypeId(
                    event.careRecipientProfileId(), event.vitalSignTypeId(), requiredHits + 1);
            var streak = recent.stream()
                    .takeWhile(reading -> threshold.get().isExceededBy(reading.getValue()))
                    .count();
            if (streak != requiredHits) {
                return;
            }
            var reading = vitalSignRepository.findById(event.vitalSignId()).orElse(recent.getFirst());
            var typeCode = vitalSignTypeRepository.findById(event.vitalSignTypeId())
                    .map(type -> type.getCode().value())
                    .orElse(null);
            eventPublisher.publishEvent(new VitalSignAnomalyDetectedIntegrationEvent(
                    event.careRecipientProfileId().value(),
                    reading.getId().value(),
                    event.vitalSignTypeId().value(),
                    typeCode,
                    threshold.get().getId().value(),
                    reading.getValue().value(),
                    threshold.get().getMinimumValue(),
                    threshold.get().getMaximumValue(),
                    event.classification().name(),
                    requiredHits,
                    event.evaluatedAt()));
        } catch (RuntimeException e) {
            log.error("Unexpected failure applying the tolerance rule to vital sign {}", event.vitalSignId().value(), e);
        }
    }
}
