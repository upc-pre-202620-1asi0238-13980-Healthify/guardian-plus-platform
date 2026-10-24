package com.healthify.guardian.platform.emergencyalerting.application.internal.eventhandlers;

import com.healthify.guardian.platform.emergencyalerting.application.commandservices.AlertCommandService;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.TriggerAlertCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertSourceType;
import com.healthify.guardian.platform.healthmonitoring.interfaces.events.VitalSignAnomalyDetectedIntegrationEvent;
import com.healthify.guardian.platform.shared.application.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Raises a {@code VITAL_SIGN_ANOMALY} alert ({@code HIGH}) when {@code Health Monitoring} confirms that a
 * vital sign stayed out of its clinical range for the required consecutive readings (US09).
 *
 * <p>The transgressed threshold is the source reference, so an anomaly streak keeps at most one active
 * alert per threshold. Never lets a failure propagate back to the publisher.</p>
 */
@Slf4j
@Service
public class VitalSignAnomalyDetectedEventHandler {

    private final AlertCommandService alertCommandService;

    public VitalSignAnomalyDetectedEventHandler(AlertCommandService alertCommandService) {
        this.alertCommandService = alertCommandService;
    }

    @EventListener
    public void on(VitalSignAnomalyDetectedIntegrationEvent event) {
        try {
            var result = alertCommandService.handle(new TriggerAlertCommand(
                    event.careRecipientProfileId(),
                    AlertSourceType.VITAL_SIGN_ANOMALY,
                    event.vitalSignThresholdId(),
                    event.detectedAt()));
            if (result instanceof Result.Failure<?, ?> failure) {
                log.warn("VITAL_SIGN_ANOMALY alert for {} could not be triggered: {}",
                        event.careRecipientProfileId(), failure.error());
            }
        } catch (RuntimeException e) {
            log.error("Unexpected failure triggering a VITAL_SIGN_ANOMALY alert for {}",
                    event.careRecipientProfileId(), e);
        }
    }
}
