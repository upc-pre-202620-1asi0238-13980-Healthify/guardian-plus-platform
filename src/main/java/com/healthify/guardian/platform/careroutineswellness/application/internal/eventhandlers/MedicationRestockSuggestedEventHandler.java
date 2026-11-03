package com.healthify.guardian.platform.careroutineswellness.application.internal.eventhandlers;

import com.healthify.guardian.platform.careroutineswellness.domain.model.events.MedicationRestockSuggestedEvent;
import com.healthify.guardian.platform.careroutineswellness.interfaces.events.MedicationRestockSuggestedIntegrationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Reacts to the internal {@link MedicationRestockSuggestedEvent} and republishes it as a
 * {@link MedicationRestockSuggestedIntegrationEvent} for consumption by {@code Emergency & Alerting}.
 */
@Service
public class MedicationRestockSuggestedEventHandler {

    private final ApplicationEventPublisher eventPublisher;

    public MedicationRestockSuggestedEventHandler(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    public void on(MedicationRestockSuggestedEvent event) {
        eventPublisher.publishEvent(new MedicationRestockSuggestedIntegrationEvent(
                event.personUnderCareId().value(), event.medicationStockId().value(), event.medicationName(),
                event.suggestedAt()));
    }
}
