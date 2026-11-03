package com.healthify.guardian.platform.iam.interfaces.rest.transform;

import com.healthify.guardian.platform.iam.domain.model.commands.ResendLoginOtpCommand;
import com.healthify.guardian.platform.iam.interfaces.rest.resources.ResendLoginOtpResource;

/**
 * Assembler converting a {@link ResendLoginOtpResource} into a {@link ResendLoginOtpCommand}.
 */
public final class ResendLoginOtpCommandFromResourceAssembler {

    private ResendLoginOtpCommandFromResourceAssembler() {
    }

    public static ResendLoginOtpCommand toCommandFromResource(ResendLoginOtpResource resource) {
        return new ResendLoginOtpCommand(resource.userAccountId());
    }
}
