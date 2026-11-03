package com.healthify.guardian.platform.emergencyalerting.application.internal.eventhandlers;

import com.healthify.guardian.platform.careroutineswellness.interfaces.events.ReminderReissuedIntegrationEvent;
import com.healthify.guardian.platform.emergencyalerting.application.commandservices.AlertCommandService;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.TriggerAlertCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertSourceType;
import com.healthify.guardian.platform.shared.application.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Raises a {@code REMINDER_REISSUED} alert ({@code MEDIUM}, primary contact only) when
 * {@code Care Routines &amp; Wellness} reissues a medication reminder that was not confirmed in time.
 *
 * <p>Registered under an explicit bean name because {@code careroutineswellness} already has a
 * handler with the same simple class name. Never lets a failure propagate back to the publisher.</p>
 */
@Slf4j
@Service("emergencyAlertingReminderReissuedEventHandler")
public class ReminderReissuedEventHandler {

    private final AlertCommandService alertCommandService;

    public ReminderReissuedEventHandler(AlertCommandService alertCommandService) {
        this.alertCommandService = alertCommandService;
    }

    @EventListener
    public void on(ReminderReissuedIntegrationEvent event) {
        try {
            var result = alertCommandService.handle(new TriggerAlertCommand(
                    event.personUnderCareId(),
                    AlertSourceType.REMINDER_REISSUED,
                    event.reminderId(),
                    event.reissuedAt()));
            if (result instanceof Result.Failure<?, ?> failure) {
                log.warn("REMINDER_REISSUED alert for {} could not be triggered: {}",
                        event.personUnderCareId(), failure.error());
            }
        } catch (RuntimeException e) {
            log.error("Unexpected failure triggering a REMINDER_REISSUED alert for {}", event.personUnderCareId(), e);
        }
    }
}
