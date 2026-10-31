package com.healthify.guardian.platform.iam.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request payload asking for a new login code (OTP) while a login is in progress.
 */
public record ResendLoginOtpResource(
        @NotNull(message = "{user-account.id.required}")
        UUID userAccountId
) {
}
