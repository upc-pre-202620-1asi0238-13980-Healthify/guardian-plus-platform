package com.healthify.guardian.platform.iam.domain.model.events;

import com.healthify.guardian.platform.iam.domain.model.valueobjects.UserAccountId;

import java.time.Instant;

/**
 * Raised when an account enables or disables the second authentication factor (OTP by e-mail).
 */
public record TwoFactorSettingChangedEvent(UserAccountId userAccountId, boolean twoFactorEnabled, Instant changedAt) {
}
