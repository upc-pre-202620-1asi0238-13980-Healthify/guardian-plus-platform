package com.healthify.guardian.platform.iam.interfaces.rest.transform;

import com.healthify.guardian.platform.iam.domain.model.commands.RequestPasswordResetCommand;
import com.healthify.guardian.platform.iam.interfaces.rest.resources.RequestPasswordResetResource;

/**
 * Assembler converting a {@link RequestPasswordResetResource} into a {@link RequestPasswordResetCommand}.
 */
public final class RequestPasswordResetCommandFromResourceAssembler {

    private RequestPasswordResetCommandFromResourceAssembler() {
    }

    public static RequestPasswordResetCommand toCommandFromResource(RequestPasswordResetResource resource) {
        return new RequestPasswordResetCommand(resource.email());
    }
}
