package com.healthify.guardian.platform.careroutineswellness.interfaces.rest.transform;

import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.ConfirmMedicationAcquisitionCommand;
import com.healthify.guardian.platform.careroutineswellness.interfaces.rest.resources.ConfirmMedicationAcquisitionResource;

import java.util.UUID;

/**
 * Assembler to convert a {@link ConfirmMedicationAcquisitionResource} to a
 * {@link ConfirmMedicationAcquisitionCommand}.
 */
public final class ConfirmMedicationAcquisitionCommandFromResourceAssembler {

    private ConfirmMedicationAcquisitionCommandFromResourceAssembler() {
    }

    /**
     * @param resource the request body; when absent, a whole package is added
     */
    public static ConfirmMedicationAcquisitionCommand toCommandFromResource(
            UUID medicationStockId, ConfirmMedicationAcquisitionResource resource) {
        return new ConfirmMedicationAcquisitionCommand(medicationStockId, resource == null ? null : resource.dosesAdded());
    }
}
