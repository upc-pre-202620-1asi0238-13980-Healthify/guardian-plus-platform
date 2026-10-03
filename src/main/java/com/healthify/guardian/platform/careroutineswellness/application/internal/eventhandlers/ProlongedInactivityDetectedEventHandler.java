package com.healthify.guardian.platform.careroutineswellness.application.internal.eventhandlers;

import com.healthify.guardian.platform.careroutineswellness.domain.model.events.ProlongedInactivityDetectedEvent;
import com.healthify.guardian.platform.careroutineswellness.interfaces.events.ProlongedInactivityDetectedIntegrationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Reacts to the internal {@link ProlongedInactivityDetectedEvent} and republishes it as a
 * {@link ProlongedInactivityDetectedIntegrationEvent} for consumption by {@code Emergency & Alerting}.
 */
@Service
public class ProlongedInactivityDetectedEventHandler {

    private final ApplicationEventPublisher eventPublisher;

    public ProlongedInactivityDetectedEventHandler(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    public void on(ProlongedInactivityDetectedEvent event) {
        eventPublisher.publishEvent(new ProlongedInactivityDetectedIntegrationEvent(
                event.personUnderCareId().value(), event.detectedAt()));
    }
}
