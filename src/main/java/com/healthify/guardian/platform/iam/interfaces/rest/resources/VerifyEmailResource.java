package com.healthify.guardian.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

/**
 * Request payload confirming the e-mail of an account with the code sent to it.
 */
public record VerifyEmailResource(
        @NotNull(message = "{user-account.id.required}")
        UUID userAccountId,

        @NotNull(message = "{otp.code.format}")
        @Pattern(regexp = "\\d{6}", message = "{otp.code.format}")
        @Schema(example = "048213")
        String code
) {
}
