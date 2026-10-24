package com.healthify.guardian.platform.iam.interfaces.rest.transform;

import com.healthify.guardian.platform.iam.application.outboundservices.SessionToken;
import com.healthify.guardian.platform.iam.interfaces.rest.resources.AuthenticatedSessionResource;

import java.util.UUID;

/**
 * Assembler converting an issued {@link SessionToken} into an {@link AuthenticatedSessionResource}.
 */
public final class AuthenticatedSessionResourceFromTokenAssembler {

    private static final String TOKEN_TYPE = "Bearer";

    private AuthenticatedSessionResourceFromTokenAssembler() {
    }

    public static AuthenticatedSessionResource toResourceFromToken(UUID userAccountId, SessionToken token) {
        return new AuthenticatedSessionResource(userAccountId, token.accessToken(), TOKEN_TYPE, token.expiresAt());
    }
}
