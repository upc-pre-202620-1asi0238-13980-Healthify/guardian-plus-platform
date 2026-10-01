package com.healthify.guardian.platform.emergencyalerting.application.internal.eventhandlers;

import com.healthify.guardian.platform.emergencyalerting.domain.model.events.AlertDispatchedEvent;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.Severity;
import com.healthify.guardian.platform.emergencyalerting.interfaces.events.EmergencyDispatchedIntegrationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Republishes the first dispatch of a {@code CRITICAL} or {@code HIGH} alert as an
 * {@link EmergencyDispatchedIntegrationEvent} for other bounded contexts.
 */
@Service
public class AlertDispatchedEventHandler {

    private final ApplicationEventPublisher eventPublisher;

    public AlertDispatchedEventHandler(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    public void on(AlertDispatchedEvent event) {
        if (!event.initialDispatch() || event.severity() == Severity.MEDIUM) {
            return;
        }
        eventPublisher.publishEvent(new EmergencyDispatchedIntegrationEvent(
                event.alertId().value(),
                event.careRecipientProfileId().value(),
                event.sourceType().name(),
                event.severity().name(),
                event.dispatchedAt()));
    }
}
