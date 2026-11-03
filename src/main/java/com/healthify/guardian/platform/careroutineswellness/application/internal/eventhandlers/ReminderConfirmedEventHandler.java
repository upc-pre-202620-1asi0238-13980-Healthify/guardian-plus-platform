package com.healthify.guardian.platform.careroutineswellness.application.internal.eventhandlers;

import com.healthify.guardian.platform.careroutineswellness.application.commandservices.MedicationStockCommandService;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.RegisterMedicationConsumptionCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.events.ReminderConfirmedEvent;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderType;
import com.healthify.guardian.platform.shared.application.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Reacts to {@link ReminderConfirmedEvent} by discounting the confirmed dose from the matching medication
 * stock (Confirm -> Consume). The stock then raises its own event, on which the Restock Policy is applied.
 *
 * <p>Confirmations with no matching stock are expected (the family has not registered that medication's
 * stock yet) and are only logged. Never lets a failure propagate back to the publisher.</p>
 */
@Slf4j
@Service
public class ReminderConfirmedEventHandler {

    private final MedicationStockCommandService medicationStockCommandService;
    private final int dosesPerConfirmation;

    public ReminderConfirmedEventHandler(
            MedicationStockCommandService medicationStockCommandService,
            @Value("${care-routines-wellness.medication-stock.doses-per-medication-confirmation:1}")
            int dosesPerConfirmation) {
        this.medicationStockCommandService = medicationStockCommandService;
        this.dosesPerConfirmation = dosesPerConfirmation;
    }

    @EventListener
    public void on(ReminderConfirmedEvent event) {
        if (event.type() != ReminderType.MEDICATION) {
            return;
        }
        try {
            var result = medicationStockCommandService.handle(new RegisterMedicationConsumptionCommand(
                    event.personUnderCareId().value(),
                    event.medicationStockId() == null ? null : event.medicationStockId().value(),
                    event.title(),
                    dosesPerConfirmation));
            if (result instanceof Result.Failure<?, ?> failure) {
                log.info("Dose of reminder {} not discounted from any stock: {}", event.reminderId().value(), failure.error());
            }
        } catch (RuntimeException e) {
            log.error("Unexpected failure discounting the dose of reminder {}", event.reminderId().value(), e);
        }
    }
}
