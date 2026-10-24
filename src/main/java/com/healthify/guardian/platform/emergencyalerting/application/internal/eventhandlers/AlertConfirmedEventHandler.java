package com.healthify.guardian.platform.emergencyalerting.application.internal.eventhandlers;

import com.healthify.guardian.platform.emergencyalerting.application.commandservices.AlertCommandService;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.DispatchAlertCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.events.AlertConfirmedEvent;
import com.healthify.guardian.platform.shared.application.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * <em>Dispatch Strategy Selector</em>: dispatches every confirmed alert at the initial level chosen
 * by {@code DispatchStrategyPolicy}.
 *
 * <p>If the dispatch fails (typically because the Fragile Citizen has no active emergency
 * contact) the alert stays active and visible to the Care Circle; the failure is logged.</p>
 */
@Slf4j
@Service
public class AlertConfirmedEventHandler {

    private final AlertCommandService alertCommandService;

    public AlertConfirmedEventHandler(AlertCommandService alertCommandService) {
        this.alertCommandService = alertCommandService;
    }

    @EventListener
    public void on(AlertConfirmedEvent event) {
        try {
            var result = alertCommandService.handle(new DispatchAlertCommand(event.alertId().value()));
            if (result instanceof Result.Failure<?, ?> failure) {
                log.warn("Alert {} of {} could not be dispatched: {}",
                        event.alertId().value(), event.careRecipientProfileId().value(), failure.error());
            }
        } catch (RuntimeException e) {
            log.error("Unexpected failure dispatching alert {}", event.alertId().value(), e);
        }
    }
}
