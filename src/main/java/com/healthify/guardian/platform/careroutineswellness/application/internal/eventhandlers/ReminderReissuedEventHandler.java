package com.healthify.guardian.platform.careroutineswellness.application.internal.eventhandlers;

import com.healthify.guardian.platform.careroutineswellness.domain.model.events.ReminderReissuedEvent;
import com.healthify.guardian.platform.careroutineswellness.interfaces.events.ReminderReissuedIntegrationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Reacts to the internal {@link ReminderReissuedEvent} and republishes it as a
 * {@link ReminderReissuedIntegrationEvent} for consumption by {@code Emergency & Alerting}.
 */
@Service
public class ReminderReissuedEventHandler {

    private final ApplicationEventPublisher eventPublisher;

    public ReminderReissuedEventHandler(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    public void on(ReminderReissuedEvent event) {
        eventPublisher.publishEvent(new ReminderReissuedIntegrationEvent(
                event.reminderId().value(),
                event.personUnderCareId().value(),
                event.type().name(),
                event.reissueCount(),
                event.reissuedAt()));
    }
}
