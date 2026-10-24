package com.healthify.guardian.platform.iam.domain.model.commands;

/**
 * Command to issue a password reset token for the account with the given e-mail. An unknown e-mail is not revealed to the requester.
 */
public record RequestPasswordResetCommand(String email) {
}
