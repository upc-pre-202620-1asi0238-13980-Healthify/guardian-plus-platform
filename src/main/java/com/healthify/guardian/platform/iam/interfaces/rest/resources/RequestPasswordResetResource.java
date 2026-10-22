package com.healthify.guardian.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request payload asking for a password reset token.
 */
public record RequestPasswordResetResource(
        @NotBlank(message = "{user-account.email.invalid}")
        @Schema(example = "ana.torres@gmail.com")
        String email
) {
}
