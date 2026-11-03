package com.healthify.guardian.platform.iam.domain.model.events;

import com.healthify.guardian.platform.iam.domain.model.valueobjects.UserAccountId;

import java.time.Instant;

/**
 * Raised when a login presents the right e-mail and password of an account that can authenticate. The login is not
 * complete yet: the second factor (OTP) must be verified before the user is authenticated.
 */
public record UserLoggedInEvent(UserAccountId userAccountId, Instant loggedInAt) {
}
