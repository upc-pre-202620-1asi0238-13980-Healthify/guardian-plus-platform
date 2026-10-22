package com.healthify.guardian.platform.iam.domain.model.commands;

/**
 * Command to set a new password with the token received by e-mail.
 */
public record ResetPasswordCommand(String token, String newRawPassword) {
}
