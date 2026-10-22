package com.healthify.guardian.platform.iam.domain.model.commands;

import java.util.UUID;

/**
 * Command to issue a one-time code to the e-mail of an account, for the given {@code OtpPurpose}.
 */
public record GenerateOtpCommand(UUID userAccountId, String purpose) {
}
