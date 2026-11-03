package com.healthify.guardian.platform.iam.interfaces.rest.resources;

import java.time.Instant;
import java.util.UUID;

/**
 * Response payload of a completed login. The app sends {@code accessToken} as {@code Authorization: Bearer <accessToken>}.
 */
public record AuthenticatedSessionResource(
        UUID userAccountId,
        String accessToken,
        String tokenType,
        Instant expiresAt
) {
}
