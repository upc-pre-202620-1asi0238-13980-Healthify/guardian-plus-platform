package com.healthify.guardian.platform.iam.interfaces.rest.transform;

import com.healthify.guardian.platform.iam.domain.model.commands.VerifyEmailCommand;
import com.healthify.guardian.platform.iam.interfaces.rest.resources.VerifyEmailResource;

/**
 * Assembler converting a {@link VerifyEmailResource} into a {@link VerifyEmailCommand}.
 */
public final class VerifyEmailCommandFromResourceAssembler {

    private VerifyEmailCommandFromResourceAssembler() {
    }

    public static VerifyEmailCommand toCommandFromResource(VerifyEmailResource resource) {
        return new VerifyEmailCommand(resource.userAccountId(), resource.code());
    }
}
