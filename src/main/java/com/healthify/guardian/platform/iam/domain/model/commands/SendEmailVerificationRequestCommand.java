package com.healthify.guardian.platform.iam.domain.model.commands;

import java.util.UUID;

/**
 * Command to send the account a new code that confirms its e-mail address. Only accounts still pending verification get one.
 */
public record SendEmailVerificationRequestCommand(UUID userAccountId) {
}
