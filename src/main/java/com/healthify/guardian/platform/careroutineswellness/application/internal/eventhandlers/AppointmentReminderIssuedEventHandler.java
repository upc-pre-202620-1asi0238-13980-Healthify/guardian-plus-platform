package com.healthify.guardian.platform.careroutineswellness.application.internal.eventhandlers;

import com.healthify.guardian.platform.careroutineswellness.domain.model.events.ReminderIssuedEvent;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderType;
import com.healthify.guardian.platform.careroutineswellness.interfaces.events.AppointmentReminderIssuedIntegrationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Reacts to the internal {@link ReminderIssuedEvent} of medical appointments and republishes it as an
 * {@link AppointmentReminderIssuedIntegrationEvent} for consumption by {@code Emergency & Alerting}, which
 * notifies the caregivers. Reminders of any other type are left untouched.
 */
@Service
public class AppointmentReminderIssuedEventHandler {

    private final ApplicationEventPublisher eventPublisher;

    public AppointmentReminderIssuedEventHandler(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    public void on(ReminderIssuedEvent event) {
        if (event.type() != ReminderType.APPOINTMENT) {
            return;
        }
        eventPublisher.publishEvent(new AppointmentReminderIssuedIntegrationEvent(
                event.reminderId().value(),
                event.personUnderCareId().value(),
                event.title(),
                event.location(),
                event.scheduledTime(),
                event.issuedAt()));
    }
}
