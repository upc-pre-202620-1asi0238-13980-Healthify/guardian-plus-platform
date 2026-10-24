package com.healthify.guardian.platform.emergencyalerting.infrastructure.scheduling;

import com.healthify.guardian.platform.emergencyalerting.application.commandservices.AlertCommandService;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.ConfirmAlertCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.FallConfirmationWindow;
import com.healthify.guardian.platform.emergencyalerting.domain.repositories.AlertRepository;
import com.healthify.guardian.platform.shared.application.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;

/**
 * Confirms every detected fall whose {@code FallConfirmationWindow} elapsed without the Fragile
 * Citizen cancelling it. Runs every second so a confirmed fall reaches the primary contact within
 * the 5 s budget of US08.
 */
@Slf4j
@Component
public class FallConfirmationTimeoutScheduler {

    private final AlertRepository alertRepository;
    private final AlertCommandService alertCommandService;
    private final Clock clock;

    public FallConfirmationTimeoutScheduler(
            AlertRepository alertRepository, AlertCommandService alertCommandService, Clock emergencyAlertingClock) {
        this.alertRepository = alertRepository;
        this.alertCommandService = alertCommandService;
        this.clock = emergencyAlertingClock;
    }

    @Scheduled(fixedDelayString = "${emergency-alerting.scheduler.fall-confirmation-check-delay-ms:1000}")
    public void confirmExpiredFalls() {
        var threshold = FallConfirmationWindow.expiredIfTriggeredBefore(clock.instant());
        for (var alert : alertRepository.findPendingConfirmationTriggeredBefore(threshold)) {
            try {
                var result = alertCommandService.handle(new ConfirmAlertCommand(alert.getId().value()));
                if (result instanceof Result.Failure<?, ?> failure) {
                    log.warn("Fall alert {} could not be confirmed: {}", alert.getId().value(), failure.error());
                }
            } catch (RuntimeException e) {
                log.error("Unexpected failure confirming fall alert {}", alert.getId().value(), e);
            }
        }
    }
}
