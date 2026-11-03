package com.healthify.guardian.platform.iam.domain.model.commands;

import java.util.UUID;

/**
 * Command to confirm the e-mail of an account with its usable {@code EMAIL_VERIFICATION} code.
 */
public record VerifyEmailCommand(UUID userAccountId, String code) {
}
