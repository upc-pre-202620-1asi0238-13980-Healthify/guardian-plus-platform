package com.healthify.guardian.platform.iam.domain.model.events;

import com.healthify.guardian.platform.iam.domain.model.valueobjects.Email;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.UserAccountId;

import java.time.Instant;

/**
 * Raised when the user confirms their e-mail address, moving the account to {@code ACTIVE}.
 */
public record EmailVerifiedEvent(UserAccountId userAccountId, Email email, Instant verifiedAt) {
}
