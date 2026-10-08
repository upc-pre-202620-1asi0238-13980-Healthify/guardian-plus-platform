package com.healthify.guardian.platform.careroutineswellness.application.internal.eventhandlers;

import com.healthify.guardian.platform.careroutineswellness.application.commandservices.MedicationStockCommandService;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.SuggestMedicationRestockCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.events.MedicationConsumedEvent;
import com.healthify.guardian.platform.shared.application.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Implements the <b>Restock Policy</b> ("whenever the remaining supply drops to 3 days or less, suggest a
 * restock") after every dose discounted from a stock: the stock is evaluated against the restock threshold
 * and raises a {@code MedicationRestockSuggestedEvent} when it is running low.
 *
 * <p>Emergency &amp; Alerting keeps a single active alert per stock, so repeated suggestions while the
 * stock stays low do not flood the family. Never lets a failure propagate back to the publisher.</p>
 */
@Slf4j
@Service
public class MedicationConsumedEventHandler {

    private final MedicationStockCommandService medicationStockCommandService;

    public MedicationConsumedEventHandler(MedicationStockCommandService medicationStockCommandService) {
        this.medicationStockCommandService = medicationStockCommandService;
    }

    @EventListener
    public void on(MedicationConsumedEvent event) {
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
