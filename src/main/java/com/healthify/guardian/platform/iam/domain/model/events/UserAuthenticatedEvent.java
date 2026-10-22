package com.healthify.guardian.platform.iam.domain.model.events;

import com.healthify.guardian.platform.iam.domain.model.valueobjects.UserAccountId;

import java.time.Instant;

/**
 * Raised when the login flow ends successfully, right before the signed session token (JWT) is issued.
 */
public record UserAuthenticatedEvent(UserAccountId userAccountId, Instant authenticatedAt) {
}
