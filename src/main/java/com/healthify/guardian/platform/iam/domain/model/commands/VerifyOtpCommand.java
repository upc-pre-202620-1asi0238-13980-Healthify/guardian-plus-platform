package com.healthify.guardian.platform.iam.domain.model.commands;

import java.util.UUID;

/**
 * Command to check the code typed by the user against their usable {@code LOGIN} code.
 */
public record VerifyOtpCommand(UUID userAccountId, String code) {
}
