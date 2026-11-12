package com.healthify.guardian.platform.emergencyalerting.application.internal.eventhandlers;

import com.healthify.guardian.platform.careroutineswellness.interfaces.events.AppointmentReminderIssuedIntegrationEvent;
import com.healthify.guardian.platform.emergencyalerting.application.internal.outboundservices.ReminderNotificationSender;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Pushes the preventive reminder of a medical appointment to the caregivers when {@code Care Routines &amp;
 * Wellness} issues it, i.e. when the time left until the appointment matches the configured lead time.
 *
 * <p>Registered under an explicit bean name because {@code careroutineswellness} already has a handler with
 * the same simple class name. Never lets a failure propagate back to the publisher.</p>
 */
@Slf4j
@Service("emergencyAlertingAppointmentReminderIssuedEventHandler")
public class AppointmentReminderIssuedEventHandler {

    private final ReminderNotificationSender reminderNotificationSender;

    public AppointmentReminderIssuedEventHandler(ReminderNotificationSender reminderNotificationSender) {
        this.reminderNotificationSender = reminderNotificationSender;
    }

    @EventListener
    public void on(AppointmentReminderIssuedIntegrationEvent event) {
        try {
            reminderNotificationSender.sendAppointmentReminder(
                    event.reminderId(),
                    new CareRecipientProfileId(event.personUnderCareId()),
                    event.title(),
                    event.location(),
                    event.scheduledTime());
        } catch (RuntimeException e) {
            log.error("Unexpected failure pushing appointment reminder {}", event.reminderId(), e);
        }
    }
}
