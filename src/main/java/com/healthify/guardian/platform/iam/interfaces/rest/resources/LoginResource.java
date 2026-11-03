package com.healthify.guardian.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request payload of the first login step: e-mail and password.
 */
public record LoginResource(
        @NotBlank(message = "{user-account.email.invalid}")
        @Schema(example = "ana.torres@gmail.com")
        String email,

        @NotBlank(message = "{user-account.password.invalid}")
        @Schema(example = "Guardian2026!")
        String password
) {
}
