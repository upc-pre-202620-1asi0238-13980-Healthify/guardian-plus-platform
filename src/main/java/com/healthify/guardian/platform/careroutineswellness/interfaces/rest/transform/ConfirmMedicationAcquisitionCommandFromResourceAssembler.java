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

    public static ConfirmMedicationAcquisitionCommand toCommandFromResource(
            UUID personUnderCareId, ConfirmMedicationAcquisitionResource resource) {
        return new ConfirmMedicationAcquisitionCommand(personUnderCareId, resource.dosesAdded());
    }
}
