package com.healthify.guardian.platform.emergencyalerting.application.internal.eventhandlers;

import com.healthify.guardian.platform.careroutineswellness.interfaces.events.MedicationRestockSuggestedIntegrationEvent;
import com.healthify.guardian.platform.emergencyalerting.application.commandservices.AlertCommandService;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.TriggerAlertCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertSourceType;
import com.healthify.guardian.platform.shared.application.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Raises a {@code MEDICATION_RESTOCK_SUGGESTED} alert ({@code MEDIUM}, primary contact only) when
 * {@code Care Routines &amp; Wellness} suggests replenishing a medication stock. While that alert is
 * active, repeated suggestions for the same stock do not raise new ones.
 *
 * <p>Registered under an explicit bean name because {@code careroutineswellness} already has a
 * handler with the same simple class name. Never lets a failure propagate back to the publisher.</p>
 */
@Slf4j
@Service("emergencyAlertingMedicationRestockSuggestedEventHandler")
public class MedicationRestockSuggestedEventHandler {

    private final AlertCommandService alertCommandService;

    public MedicationRestockSuggestedEventHandler(AlertCommandService alertCommandService) {
        this.alertCommandService = alertCommandService;
    }

    @EventListener
    public void on(MedicationRestockSuggestedIntegrationEvent event) {
        try {
            var result = alertCommandService.handle(new TriggerAlertCommand(
                    event.personUnderCareId(),
                    AlertSourceType.MEDICATION_RESTOCK_SUGGESTED,
                    event.medicationStockId(),
                    event.suggestedAt()));
            if (result instanceof Result.Failure<?, ?> failure) {
                log.warn("MEDICATION_RESTOCK_SUGGESTED alert for {} could not be triggered: {}",
                        event.personUnderCareId(), failure.error());
            }
        } catch (RuntimeException e) {
            log.error("Unexpected failure triggering a MEDICATION_RESTOCK_SUGGESTED alert for {}",
                    event.personUnderCareId(), e);
        }
    }
}
