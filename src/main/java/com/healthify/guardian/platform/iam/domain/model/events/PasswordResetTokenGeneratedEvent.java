package com.healthify.guardian.platform.iam.domain.model.events;

import com.healthify.guardian.platform.iam.domain.model.valueobjects.Email;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.UserAccountId;

import java.time.Instant;

/**
 * Raised when an account generates a password reset token. It carries no token: only its hash is kept.
 */
public record PasswordResetTokenGeneratedEvent(UserAccountId userAccountId, Email email, Instant expiresAt) {
}
