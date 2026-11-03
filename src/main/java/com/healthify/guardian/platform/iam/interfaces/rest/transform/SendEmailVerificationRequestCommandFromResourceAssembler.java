package com.healthify.guardian.platform.iam.interfaces.rest.transform;

import com.healthify.guardian.platform.iam.domain.model.commands.SendEmailVerificationRequestCommand;
import com.healthify.guardian.platform.iam.interfaces.rest.resources.SendEmailVerificationRequestResource;

/**
 * Assembler converting a {@link SendEmailVerificationRequestResource} into a {@link SendEmailVerificationRequestCommand}.
 */
public final class SendEmailVerificationRequestCommandFromResourceAssembler {

    private SendEmailVerificationRequestCommandFromResourceAssembler() {
    }

    public static SendEmailVerificationRequestCommand toCommandFromResource(SendEmailVerificationRequestResource resource) {
        return new SendEmailVerificationRequestCommand(resource.userAccountId());
    }
}
