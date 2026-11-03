package com.healthify.guardian.platform.iam.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request payload asking for a new e-mail verification code.
 */
public record SendEmailVerificationRequestResource(
        @NotNull(message = "{user-account.id.required}")
        UUID userAccountId
) {
}
