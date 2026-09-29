package com.healthify.guardian.platform.careroutineswellness.application.internal.eventhandlers;

import com.healthify.guardian.platform.careroutineswellness.domain.model.events.ReminderConfirmedEvent;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderType;
import com.healthify.guardian.platform.careroutineswellness.domain.repositories.MedicationStockRepository;
import com.healthify.guardian.platform.careroutineswellness.domain.services.MedicationStockPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Reacts to the internal {@link ReminderConfirmedEvent}. When the confirmed reminder is a
 * medication reminder, registers the corresponding dose consumption in {@code MedicationStock}
 * and evaluates {@code MedicationStockPolicy} to decide whether a restock must be suggested.
 *
 * <p>Silently ignores confirmations for a person under care that has no medication stock yet:
 * this can only happen if medication reminders are being scheduled before the first package
 * acquisition has been confirmed for them.</p>
 */
@Service
public class ReminderConfirmedEventHandler {

    private final MedicationStockRepository medicationStockRepository;
    private final MedicationStockPolicy medicationStockPolicy;
    private final int dosesPerConfirmation;

    public ReminderConfirmedEventHandler(
            MedicationStockRepository medicationStockRepository,
            MedicationStockPolicy medicationStockPolicy,
            @Value("${care-routines-wellness.medication-stock.doses-per-medication-confirmation:1}")
            int dosesPerConfirmation) {
        this.medicationStockRepository = medicationStockRepository;
        this.medicationStockPolicy = medicationStockPolicy;
        this.dosesPerConfirmation = dosesPerConfirmation;
    }

    @EventListener
    public void on(ReminderConfirmedEvent event) {
        if (event.type() != ReminderType.MEDICATION) {
            return;
        }

        medicationStockRepository.findByPersonUnderCareId(event.personUnderCareId())
                .ifPresent(stock -> {
                    stock.registerConsumption(dosesPerConfirmation);
                    if (medicationStockPolicy.requiresRestockSuggestion(stock)) {
                        stock.suggestRestock();
                    }
                    medicationStockRepository.save(stock);
                });
    }
}
