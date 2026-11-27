package com.healthify.guardian.platform.iam.domain.model.events;

import com.healthify.guardian.platform.iam.domain.model.valueobjects.Email;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.UserAccountId;

import java.time.Instant;

/**
 * Raised when an account is created from a Google sign-in. Google already proved the e-mail, so the account starts
 * {@code ACTIVE} and no verification code is sent.
 */
public record UserSignedUpWithGoogleEvent(UserAccountId userAccountId, Email email, Instant signedUpAt) {
}
