package com.healthify.guardian.platform.iam.domain.model.commands;

import java.util.UUID;

/**
 * Command to stop requiring the OTP on login.
 */
public record DisableTwoFactorCommand(UUID userAccountId) {
}
