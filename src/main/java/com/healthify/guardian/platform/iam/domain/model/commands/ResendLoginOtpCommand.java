package com.healthify.guardian.platform.iam.domain.model.commands;

import java.util.UUID;

/**
 * Command to send a new second-factor code (OTP) to an account that is in the middle of a login, i.e. that passed
 * the password step recently. It never starts a login on its own.
 */
public record ResendLoginOtpCommand(UUID userAccountId) {
}
