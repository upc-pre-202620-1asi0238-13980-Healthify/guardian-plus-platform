package com.healthify.guardian.platform.iam.interfaces.rest.transform;

import com.healthify.guardian.platform.iam.domain.model.commands.RegisterUserCredentialsCommand;
import com.healthify.guardian.platform.iam.interfaces.rest.resources.RegisterUserCredentialsResource;

/**
 * Assembler converting a {@link RegisterUserCredentialsResource} into a {@link RegisterUserCredentialsCommand}.
 */
public final class RegisterUserCredentialsCommandFromResourceAssembler {

    private RegisterUserCredentialsCommandFromResourceAssembler() {
    }

    public static RegisterUserCredentialsCommand toCommandFromResource(RegisterUserCredentialsResource resource) {
        return new RegisterUserCredentialsCommand(resource.email(), resource.password());
    }
}
