package com.healthify.guardian.platform.careroutineswellness.interfaces.rest.transform;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.MedicationStock;
import com.healthify.guardian.platform.careroutineswellness.interfaces.rest.resources.MedicationStockResource;

import java.time.LocalDate;

/**
 * Assembler that converts a {@link MedicationStock} domain aggregate into a
 * {@link MedicationStockResource}.
 */
public final class MedicationStockResourceFromEntityAssembler {

    private MedicationStockResourceFromEntityAssembler() {
    }

    /**
     * @param today              the current date in the person's zone, to project the depletion date
     * @param restockRecommended whether the stock is at or below the restock threshold
     */
    public static MedicationStockResource toResourceFromEntity(MedicationStock stock, LocalDate today, boolean restockRecommended) {
        return new MedicationStockResource(
                stock.getId().value(),
                stock.getPersonUnderCareId().value(),
                stock.getMedication().name(),
                stock.getMedication().dosage(),
                stock.getRemainingDoses(),
                stock.getDailyConsumption(),
                stock.getPackageSize(),
                stock.getLastAcquisitionDate(),
                stock.remainingDaysOfSupply(),
                stock.estimatedDepletionDate(today),
                restockRecommended);
    }
}
