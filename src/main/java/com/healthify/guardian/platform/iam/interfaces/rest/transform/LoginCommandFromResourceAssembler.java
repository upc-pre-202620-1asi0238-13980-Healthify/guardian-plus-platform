package com.healthify.guardian.platform.iam.interfaces.rest.transform;

import com.healthify.guardian.platform.iam.domain.model.commands.LoginCommand;
import com.healthify.guardian.platform.iam.interfaces.rest.resources.LoginResource;

/**
 * Assembler converting a {@link LoginResource} into a {@link LoginCommand}.
 */
public final class LoginCommandFromResourceAssembler {

    private LoginCommandFromResourceAssembler() {
    }

    public static LoginCommand toCommandFromResource(LoginResource resource) {
        return new LoginCommand(resource.email(), resource.password());
    }
}
