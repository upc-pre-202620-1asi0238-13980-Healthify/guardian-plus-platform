package com.healthify.guardian.platform.healthmonitoring.application.internal.eventhandlers;

import com.healthify.guardian.platform.healthmonitoring.domain.model.events.WeeklySummaryCompiledEvent;
import com.healthify.guardian.platform.healthmonitoring.interfaces.events.HealthReportCompiledIntegrationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Reacts to {@link WeeklySummaryCompiledEvent} by announcing the new report to supporting
 * contexts through {@link HealthReportCompiledIntegrationEvent}.
 */
@Service
public class WeeklySummaryCompiledEventHandler {

    private final ApplicationEventPublisher eventPublisher;

    public WeeklySummaryCompiledEventHandler(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    public void on(WeeklySummaryCompiledEvent event) {
        eventPublisher.publishEvent(new HealthReportCompiledIntegrationEvent(
                event.healthReportId().value(),
                event.careRecipientProfileId().value(),
                event.period().startDate(),
                event.period().endDate(),
                event.recurrentAnomaliesCount(),
                event.compiledAt()));
    }
}
