package com.healthify.guardian.platform.emergencyalerting.application.internal.eventhandlers;

import com.healthify.guardian.platform.emergencyalerting.application.commandservices.AlertCommandService;
import com.healthify.guardian.platform.emergencyalerting.application.queryservices.AlertQueryService;
import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.Alert;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.ResolveAlertCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.events.IncidentClosedEvent;
import com.healthify.guardian.platform.emergencyalerting.domain.model.queries.GetAlertByIdQuery;
import com.healthify.guardian.platform.emergencyalerting.interfaces.events.IncidentClosedIntegrationEvent;
import com.healthify.guardian.platform.shared.application.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Resolves the alert of a closed incident and publishes {@link IncidentClosedIntegrationEvent} so
 * the incident can be added to the Fragile Citizen's health history.
 */
@Slf4j
@Service
public class IncidentClosedEventHandler {

    private final AlertCommandService alertCommandService;
    private final AlertQueryService alertQueryService;
    private final ApplicationEventPublisher eventPublisher;

    public IncidentClosedEventHandler(
            AlertCommandService alertCommandService,
            AlertQueryService alertQueryService,
            ApplicationEventPublisher eventPublisher) {
        this.alertCommandService = alertCommandService;
        this.alertQueryService = alertQueryService;
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    public void on(IncidentClosedEvent event) {
        try {
            var resolved = alertCommandService.handle(new ResolveAlertCommand(event.alertId().value()));
            var alert = switch (resolved) {
                case Result.Success<Alert, ?> success -> success.value();
                case Result.Failure<Alert, ?> failure -> {
                    log.warn("Alert {} could not be resolved after closing incident {}",
                            event.alertId().value(), event.incidentId().value());
                    yield alertQueryService.handle(new GetAlertByIdQuery(event.alertId())).orElse(null);
                }
            };
            if (alert == null) {
                return;
            }
            eventPublisher.publishEvent(new IncidentClosedIntegrationEvent(
                    event.incidentId().value(),
                    event.alertId().value(),
                    alert.getCareRecipientProfileId().value(),
                    alert.getSource().sourceType().name(),
                    event.notes(),
                    event.closedAt()));
        } catch (RuntimeException e) {
            log.error("Unexpected failure handling the closure of incident {}", event.incidentId().value(), e);
        }
    }
}
