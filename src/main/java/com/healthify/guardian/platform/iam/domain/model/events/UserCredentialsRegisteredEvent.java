package com.healthify.guardian.platform.iam.domain.model.events;

import com.healthify.guardian.platform.iam.domain.model.valueobjects.Email;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.UserAccountId;

import java.time.Instant;

/**
 * Raised when new credentials are registered. The account starts in {@code PENDING_EMAIL_VERIFICATION}, which
 * triggers the e-mail verification code.
 */
public record UserCredentialsRegisteredEvent(UserAccountId userAccountId, Email email, Instant registeredAt) {
}
