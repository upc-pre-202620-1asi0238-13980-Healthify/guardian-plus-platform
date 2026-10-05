package com.healthify.guardian.platform.healthmonitoring.interfaces.rest.transform;

import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.RegisterVitalSignTypeCommand;
import com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources.RegisterVitalSignTypeResource;

/**
 * Assembler converting a {@link RegisterVitalSignTypeResource} into a {@link RegisterVitalSignTypeCommand}.
 */
public final class RegisterVitalSignTypeCommandFromResourceAssembler {

    private RegisterVitalSignTypeCommandFromResourceAssembler() {
    }

    public static RegisterVitalSignTypeCommand toCommandFromResource(RegisterVitalSignTypeResource resource) {
        return new RegisterVitalSignTypeCommand(resource.code(), resource.name(), resource.unit());
    }
}
