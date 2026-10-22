package com.healthify.guardian.platform.iam.domain.model.commands;

import java.util.UUID;

/**
 * Command to require an OTP sent by e-mail on every login of the account.
 */
public record EnableTwoFactorCommand(UUID userAccountId) {
}
