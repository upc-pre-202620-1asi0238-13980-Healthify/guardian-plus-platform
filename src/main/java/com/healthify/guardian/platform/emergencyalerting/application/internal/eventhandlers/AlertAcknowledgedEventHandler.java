package com.healthify.guardian.platform.emergencyalerting.application.internal.eventhandlers;

import com.healthify.guardian.platform.emergencyalerting.application.commandservices.IncidentCommandService;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.OpenIncidentCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.events.AlertAcknowledgedEvent;
import com.healthify.guardian.platform.shared.application.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * <em>Escalation Stopper</em>: once an alert is acknowledged it no longer awaits acknowledgement,
 * so the escalation scheduler skips it; this handler then opens its incident in attention.
 */
@Slf4j
@Service
public class AlertAcknowledgedEventHandler {

    private final IncidentCommandService incidentCommandService;

    public AlertAcknowledgedEventHandler(IncidentCommandService incidentCommandService) {
        this.incidentCommandService = incidentCommandService;
    }

    @EventListener
    public void on(AlertAcknowledgedEvent event) {
        try {
            var result = incidentCommandService.handle(
                    new OpenIncidentCommand(event.alertId().value(), event.acknowledgedAt()));
            if (result instanceof Result.Failure<?, ?> failure) {
                log.warn("Incident for alert {} could not be opened: {}", event.alertId().value(), failure.error());
            }
        } catch (RuntimeException e) {
            log.error("Unexpected failure opening the incident of alert {}", event.alertId().value(), e);
        }
    }
}
