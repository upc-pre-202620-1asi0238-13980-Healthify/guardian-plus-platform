package com.healthify.guardian.platform.iam.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload setting a new password with the reset token received by e-mail.
 */
public record ResetPasswordResource(
        @NotBlank(message = "{password-reset-token.required}")
        String token,

        @NotBlank(message = "{user-account.password.invalid}")
        @Size(min = 8, max = 72, message = "{user-account.password.invalid}")
        String newPassword
) {
}
