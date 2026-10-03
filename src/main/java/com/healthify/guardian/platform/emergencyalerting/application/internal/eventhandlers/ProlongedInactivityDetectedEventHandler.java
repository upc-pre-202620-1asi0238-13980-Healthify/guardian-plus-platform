package com.healthify.guardian.platform.emergencyalerting.application.internal.eventhandlers;

import com.healthify.guardian.platform.careroutineswellness.interfaces.events.ProlongedInactivityDetectedIntegrationEvent;
import com.healthify.guardian.platform.emergencyalerting.application.commandservices.AlertCommandService;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.TriggerAlertCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertSourceType;
import com.healthify.guardian.platform.shared.application.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Raises a {@code PROLONGED_INACTIVITY} alert ({@code HIGH}) when {@code Care Routines &amp; Wellness}
 * detects prolonged inactivity.
 *
 * <p>The integration event carries no activity monitor id yet (decision D6 of the plan), so the
 * person under care's id is used as the source reference. As a side effect, a person has at most
 * one active inactivity alert at a time.</p>
 *
 * <p>Registered under an explicit bean name because {@code careroutineswellness} already has a
 * handler with the same simple class name. Never lets a failure propagate back to the publisher.</p>
 */
@Slf4j
@Service("emergencyAlertingProlongedInactivityDetectedEventHandler")
public class ProlongedInactivityDetectedEventHandler {

    private final AlertCommandService alertCommandService;

    public ProlongedInactivityDetectedEventHandler(AlertCommandService alertCommandService) {
        this.alertCommandService = alertCommandService;
    }

    @EventListener
    public void on(ProlongedInactivityDetectedIntegrationEvent event) {
        try {
            var result = alertCommandService.handle(new TriggerAlertCommand(
                    event.personUnderCareId(),
                    AlertSourceType.PROLONGED_INACTIVITY,
                    event.personUnderCareId(),
                    event.detectedAt()));
            if (result instanceof Result.Failure<?, ?> failure) {
                log.warn("PROLONGED_INACTIVITY alert for {} could not be triggered: {}",
                        event.personUnderCareId(), failure.error());
            }
        } catch (RuntimeException e) {
            log.error("Unexpected failure triggering a PROLONGED_INACTIVITY alert for {}",
                    event.personUnderCareId(), e);
        }
    }
}
