package com.healthify.guardian.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

/**
 * Request payload with the second-factor code (OTP) of a login.
 */
public record VerifyOtpResource(
        @NotNull(message = "{user-account.id.required}")
        UUID userAccountId,

        @NotNull(message = "{otp.code.format}")
        @Pattern(regexp = "\\d{6}", message = "{otp.code.format}")
        @Schema(example = "048213")
        String code
) {
}
