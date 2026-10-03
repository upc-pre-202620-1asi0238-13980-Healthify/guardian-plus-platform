package com.healthify.guardian.platform.careroutineswellness.interfaces.rest.transform;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.MedicationStock;
import com.healthify.guardian.platform.careroutineswellness.interfaces.rest.resources.MedicationStockResource;

/**
 * Assembler that converts a {@link MedicationStock} domain aggregate into a
 * {@link MedicationStockResource}.
 */
public final class MedicationStockResourceFromEntityAssembler {

    private MedicationStockResourceFromEntityAssembler() {
    }

    public static MedicationStockResource toResourceFromEntity(MedicationStock stock) {
        return new MedicationStockResource(
                stock.getId().value(),
                stock.getPersonUnderCareId().value(),
                stock.getRemainingDoses(),
                stock.getDailyConsumption(),
                stock.getLastAcquisitionDate(),
                stock.remainingDaysOfSupply());
    }
}
