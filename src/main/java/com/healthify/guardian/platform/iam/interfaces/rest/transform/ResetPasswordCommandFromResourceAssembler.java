package com.healthify.guardian.platform.iam.interfaces.rest.transform;

import com.healthify.guardian.platform.iam.domain.model.commands.ResetPasswordCommand;
import com.healthify.guardian.platform.iam.interfaces.rest.resources.ResetPasswordResource;

/**
 * Assembler converting a {@link ResetPasswordResource} into a {@link ResetPasswordCommand}.
 */
public final class ResetPasswordCommandFromResourceAssembler {

    private ResetPasswordCommandFromResourceAssembler() {
    }

    public static ResetPasswordCommand toCommandFromResource(ResetPasswordResource resource) {
        return new ResetPasswordCommand(resource.token(), resource.newPassword());
    }
}
