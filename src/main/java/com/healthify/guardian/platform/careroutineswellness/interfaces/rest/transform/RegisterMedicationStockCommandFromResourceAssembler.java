package com.healthify.guardian.platform.careroutineswellness.interfaces.rest.transform;

import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.RegisterMedicationStockCommand;
import com.healthify.guardian.platform.careroutineswellness.interfaces.rest.resources.RegisterMedicationStockResource;

/**
 * Assembler to convert a {@link RegisterMedicationStockResource} to a {@link RegisterMedicationStockCommand}.
 */
public final class RegisterMedicationStockCommandFromResourceAssembler {

    private RegisterMedicationStockCommandFromResourceAssembler() {
    }

    public static RegisterMedicationStockCommand toCommandFromResource(RegisterMedicationStockResource resource) {
        return new RegisterMedicationStockCommand(
                resource.personUnderCareId(),
                resource.medicationName(),
                resource.dosage(),
                resource.dailyConsumption(),
                resource.packageSize(),
                resource.initialDoses());
    }
}
