package com.healthify.guardian.platform.iam.domain.model.commands;

import java.util.UUID;

/**
 * Command to send the second-factor code (OTP) by e-mail after the account logged in with the right e-mail and password.
 */
public record TriggerTwoFactorCommand(UUID userAccountId) {
}
