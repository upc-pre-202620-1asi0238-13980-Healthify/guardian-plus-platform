package com.healthify.guardian.platform.iam.interfaces.rest.transform;

import com.healthify.guardian.platform.iam.domain.model.commands.SignInWithGoogleCommand;
import com.healthify.guardian.platform.iam.interfaces.rest.resources.SignInWithGoogleResource;

/**
 * Assembler converting a {@link SignInWithGoogleResource} into a {@link SignInWithGoogleCommand}.
 */
public final class SignInWithGoogleCommandFromResourceAssembler {

    private SignInWithGoogleCommandFromResourceAssembler() {
    }

    public static SignInWithGoogleCommand toCommandFromResource(SignInWithGoogleResource resource) {
        return new SignInWithGoogleCommand(resource.idToken());
    }
}
