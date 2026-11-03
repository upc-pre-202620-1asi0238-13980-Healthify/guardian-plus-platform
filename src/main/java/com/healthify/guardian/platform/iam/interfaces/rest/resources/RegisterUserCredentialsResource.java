package com.healthify.guardian.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload registering the credentials of a new caregiver or family member.
 */
public record RegisterUserCredentialsResource(
        @NotBlank(message = "{user-account.email.invalid}")
        @Schema(example = "ana.torres@gmail.com")
        String email,

        @NotBlank(message = "{user-account.password.invalid}")
        @Size(min = 8, max = 72, message = "{user-account.password.invalid}")
        @Schema(example = "Guardian2026!")
        String password
) {
}
