package com.healthify.guardian.platform.iam.interfaces.rest.resources;

import java.util.UUID;

/**
 * Response payload of a user account. Never carries a password or secret hash.
 */
public record UserAccountResource(
        UUID id,
        String email,
        String status,
        boolean emailVerified
) {
}
