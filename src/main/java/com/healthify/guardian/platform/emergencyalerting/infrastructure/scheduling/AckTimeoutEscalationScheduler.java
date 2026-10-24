package com.healthify.guardian.platform.emergencyalerting.infrastructure.scheduling;

import com.healthify.guardian.platform.emergencyalerting.application.commandservices.AlertCommandService;
import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.Alert;
import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.AlertSettings;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.BroadcastAlertCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.EscalateAlertCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.RecipientLevel;
import com.healthify.guardian.platform.emergencyalerting.domain.repositories.AlertRepository;
import com.healthify.guardian.platform.emergencyalerting.domain.repositories.AlertSettingsRepository;
import com.healthify.guardian.platform.emergencyalerting.domain.repositories.EmergencyContactRepository;
import com.healthify.guardian.platform.emergencyalerting.domain.services.EscalationPolicy;
import com.healthify.guardian.platform.shared.application.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Escalates every dispatched alert nobody acknowledged within the Fragile Citizen's
 * acknowledgement timeout since its last dispatch: to the secondary contacts first and, once the
 * chain is exhausted, to every active contact (<em>Critical Broadcast Fallback</em>).
 * {@code EscalationPolicy} decides the next level.
 */
@Slf4j
@Component
public class AckTimeoutEscalationScheduler {

    private final AlertRepository alertRepository;
    private final AlertSettingsRepository alertSettingsRepository;
    private final EmergencyContactRepository emergencyContactRepository;
    private final EscalationPolicy escalationPolicy;
    private final AlertCommandService alertCommandService;
    private final Clock clock;

    public AckTimeoutEscalationScheduler(
            AlertRepository alertRepository,
            AlertSettingsRepository alertSettingsRepository,
            EmergencyContactRepository emergencyContactRepository,
            EscalationPolicy escalationPolicy,
            AlertCommandService alertCommandService,
            Clock emergencyAlertingClock) {
        this.alertRepository = alertRepository;
        this.alertSettingsRepository = alertSettingsRepository;
        this.emergencyContactRepository = emergencyContactRepository;
        this.escalationPolicy = escalationPolicy;
        this.alertCommandService = alertCommandService;
        this.clock = emergencyAlertingClock;
    }

    @Scheduled(fixedDelayString = "${emergency-alerting.scheduler.ack-timeout-check-delay-ms:5000}")
    public void escalateUnacknowledgedAlerts() {
        var now = clock.instant();
        Map<CareRecipientProfileId, AlertSettings> settingsByCareRecipient = new HashMap<>();
        for (var alert : alertRepository.findAwaitingAcknowledgement()) {
            try {
                var settings = settingsByCareRecipient.computeIfAbsent(
                        alert.getCareRecipientProfileId(), this::settingsFor);
                escalateIfExpired(alert, settings, now);
            } catch (RuntimeException e) {
                log.error("Unexpected failure escalating alert {}", alert.getId().value(), e);
            }
        }
    }

    /** Returns the Fragile Citizen's settings, or the defaults if they never changed them. */
    private AlertSettings settingsFor(CareRecipientProfileId careRecipientProfileId) {
        return alertSettingsRepository.findByCareRecipientProfileId(careRecipientProfileId)
                .orElseGet(() -> new AlertSettings(careRecipientProfileId));
    }

    private void escalateIfExpired(Alert alert, AlertSettings settings, Instant now) {
        if (!alert.isAckTimeoutExpired(settings.getPrimaryAckTimeout(), now)) {
            return;
        }
        var contacts = emergencyContactRepository
                .findActiveByCareRecipientProfileIdOrderByPriority(alert.getCareRecipientProfileId());
        var nextLevel = escalationPolicy.resolveNextLevel(alert, settings, contacts);
        if (nextLevel.isEmpty()) {
            return;
        }
        var alertId = alert.getId().value();
        var result = nextLevel.get() == RecipientLevel.SECONDARY
                ? alertCommandService.handle(new EscalateAlertCommand(alertId))
                : alertCommandService.handle(new BroadcastAlertCommand(alertId));
        if (result instanceof Result.Failure<?, ?> failure) {
            log.warn("Alert {} could not move to {}: {}", alertId, nextLevel.get(), failure.error());
        }
    }
}
