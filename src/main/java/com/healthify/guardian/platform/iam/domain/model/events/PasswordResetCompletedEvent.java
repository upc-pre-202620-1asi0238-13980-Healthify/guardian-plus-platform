package com.healthify.guardian.platform.iam.domain.model.events;

import com.healthify.guardian.platform.iam.domain.model.valueobjects.PasswordResetTokenId;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.UserAccountId;

import java.time.Instant;

/**
 * Raised when a valid token is redeemed to set a new password; the token is consumed at that moment.
 */
public record PasswordResetCompletedEvent(PasswordResetTokenId passwordResetTokenId, UserAccountId userAccountId, Instant completedAt) {
}
