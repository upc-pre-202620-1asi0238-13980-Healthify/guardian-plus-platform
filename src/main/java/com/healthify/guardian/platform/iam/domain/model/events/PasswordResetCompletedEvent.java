package com.healthify.guardian.platform.iam.domain.model.events;

import com.healthify.guardian.platform.iam.domain.model.valueobjects.UserAccountId;

import java.time.Instant;

/**
 * Raised when an account sets a new password with a valid reset token, which is consumed at that moment.
 */
public record PasswordResetCompletedEvent(UserAccountId userAccountId, Instant completedAt) {
}
