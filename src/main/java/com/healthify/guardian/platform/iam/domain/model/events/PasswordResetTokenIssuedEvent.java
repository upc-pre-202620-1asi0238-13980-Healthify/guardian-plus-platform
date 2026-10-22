package com.healthify.guardian.platform.iam.domain.model.events;

import com.healthify.guardian.platform.iam.domain.model.valueobjects.PasswordResetTokenId;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.UserAccountId;

import java.time.Instant;

/**
 * Raised when a password reset token is issued. It carries no token: only its hash is kept by the aggregate.
 */
public record PasswordResetTokenIssuedEvent(PasswordResetTokenId passwordResetTokenId, UserAccountId userAccountId, Instant expiresAt) {
}
