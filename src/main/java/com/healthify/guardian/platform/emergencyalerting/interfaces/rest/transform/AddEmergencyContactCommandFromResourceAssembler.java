package com.healthify.guardian.platform.emergencyalerting.interfaces.rest.transform;

import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.AddEmergencyContactCommand;
import com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources.AddEmergencyContactResource;

/**
 * Assembler to convert an {@link AddEmergencyContactResource} to an {@link AddEmergencyContactCommand}.
 */
public final class AddEmergencyContactCommandFromResourceAssembler {

    private AddEmergencyContactCommandFromResourceAssembler() {
    }

    public static AddEmergencyContactCommand toCommandFromResource(AddEmergencyContactResource resource) {
        return new AddEmergencyContactCommand(
                resource.careRecipientProfileId(),
                resource.userId(),
                resource.displayName(),
                resource.relationship(),
                resource.phoneNumber(),
                resource.priorityOrder());
    }
}
