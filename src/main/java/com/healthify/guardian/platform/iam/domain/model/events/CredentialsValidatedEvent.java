package com.healthify.guardian.platform.iam.domain.model.events;

import com.healthify.guardian.platform.iam.domain.model.valueobjects.UserAccountId;

import java.time.Instant;

/**
 * Raised when a login attempt presents the right e-mail and password for an account that can authenticate.
 */
public record CredentialsValidatedEvent(UserAccountId userAccountId, boolean requiresSecondFactor, Instant validatedAt) {
}
