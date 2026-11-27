package com.healthify.guardian.platform.emergencyalerting.application.internal.eventhandlers;

import com.healthify.guardian.platform.emergencyalerting.application.commandservices.AlertCommandService;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.TriggerAlertCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertSourceType;
import com.healthify.guardian.platform.mobilitygeofencing.interfaces.events.SafeZoneViolationIntegrationEvent;
import com.healthify.guardian.platform.shared.application.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Raises a {@code SAFE_ZONE_VIOLATION} alert ({@code HIGH}) when {@code Mobility &amp; Geofencing} receives a
 * location outside the person's active safe zone.
 *
 * <p>Mobility publishes the violation for every location received outside the zone, so the safe zone is used
 * as the source reference: while its alert is active, the next violations return it instead of raising a new
 * one.</p>
 *
 * <p>Registered under an explicit bean name because {@code mobilitygeofencing} already has a handler with the
 * same simple class name. Never lets a failure propagate back to the publisher.</p>
 */
@Slf4j
@Service("emergencyAlertingSafeZoneViolationDetectedEventHandler")
public class SafeZoneViolationDetectedEventHandler {

    private final AlertCommandService alertCommandService;

    public SafeZoneViolationDetectedEventHandler(AlertCommandService alertCommandService) {
        this.alertCommandService = alertCommandService;
    }

    @EventListener
    public void on(SafeZoneViolationIntegrationEvent event) {
        try {
            var result = alertCommandService.handle(new TriggerAlertCommand(
                    event.careRecipientProfileId(),
                    AlertSourceType.SAFE_ZONE_VIOLATION,
                    event.safeZoneId(),
                    event.occurredAt()));
            if (result instanceof Result.Failure<?, ?> failure) {
                log.warn("SAFE_ZONE_VIOLATION alert for {} could not be triggered: {}",
                        event.careRecipientProfileId(), failure.error());
            }
        } catch (RuntimeException e) {
            log.error("Unexpected failure triggering a SAFE_ZONE_VIOLATION alert for {}",
                    event.careRecipientProfileId(), e);
        }
    }
}
