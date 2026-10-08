package com.healthify.guardian.platform.careroutineswellness.application.internal.eventhandlers;

import com.healthify.guardian.platform.careroutineswellness.application.commandservices.MedicationStockCommandService;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.SuggestMedicationRestockCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.events.MedicationStockRegisteredEvent;
import com.healthify.guardian.platform.shared.application.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Applies the <b>Restock Policy</b> to a newly registered stock, so a medication registered with only a few
 * doses at hand is flagged right away instead of after its first confirmed dose.
 *
 * <p>Never lets a failure propagate back to the publisher.</p>
 */
@Slf4j
@Service
public class MedicationStockRegisteredEventHandler {

    private final MedicationStockCommandService medicationStockCommandService;

    public MedicationStockRegisteredEventHandler(MedicationStockCommandService medicationStockCommandService) {
        this.medicationStockCommandService = medicationStockCommandService;
    }

    @EventListener
    public void on(MedicationStockRegisteredEvent event) {
        var medicationStockId = event.medicationStockId().value();
        try {
            var result = medicationStockCommandService.handle(new SuggestMedicationRestockCommand(medicationStockId));
            if (result instanceof Result.Failure<?, ?> failure) {
                log.warn("Restock evaluation of stock {} failed: {}", medicationStockId, failure.error());
            }
        } catch (RuntimeException e) {
            log.error("Unexpected failure evaluating the restock of stock {}", medicationStockId, e);
        }
    }
}
