package com.healthify.guardian.platform.emergencyalerting.application.internal.eventhandlers;

import com.healthify.guardian.platform.emergencyalerting.application.commandservices.AlertCommandService;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.ConfirmAlertCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.events.AlertTriggeredEvent;
import com.healthify.guardian.platform.shared.application.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Confirms right away every alert whose source grants no confirmation window (SOS, biometric
 * anomaly, safe zone, inactivity, routine). Detected falls are left pending for
 * {@code FallConfirmationTimeoutScheduler}.
 */
@Slf4j
@Service
public class AlertTriggeredEventHandler {

    private final AlertCommandService alertCommandService;

    public AlertTriggeredEventHandler(AlertCommandService alertCommandService) {
        this.alertCommandService = alertCommandService;
    }

    @EventListener
    public void on(AlertTriggeredEvent event) {
        if (event.source().requiresConfirmationWindow()) {
            return;
        }
        try {
            var result = alertCommandService.handle(new ConfirmAlertCommand(event.alertId().value()));
            if (result instanceof Result.Failure<?, ?> failure) {
                log.warn("Alert {} could not be confirmed: {}", event.alertId().value(), failure.error());
            }
        } catch (RuntimeException e) {
            log.error("Unexpected failure confirming alert {}", event.alertId().value(), e);
        }
    }
}
