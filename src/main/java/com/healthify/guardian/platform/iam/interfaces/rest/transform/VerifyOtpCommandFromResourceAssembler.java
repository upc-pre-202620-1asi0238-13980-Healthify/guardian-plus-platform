package com.healthify.guardian.platform.iam.interfaces.rest.transform;

import com.healthify.guardian.platform.iam.domain.model.commands.VerifyOtpCommand;
import com.healthify.guardian.platform.iam.interfaces.rest.resources.VerifyOtpResource;

/**
 * Assembler converting a {@link VerifyOtpResource} into a {@link VerifyOtpCommand}.
 */
public final class VerifyOtpCommandFromResourceAssembler {

    private VerifyOtpCommandFromResourceAssembler() {
    }

    public static VerifyOtpCommand toCommandFromResource(VerifyOtpResource resource) {
        return new VerifyOtpCommand(resource.userAccountId(), resource.code());
    }
}
