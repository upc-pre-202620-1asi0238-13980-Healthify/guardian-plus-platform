package com.healthify.guardian.platform.iam.domain.model.events;

import com.healthify.guardian.platform.iam.domain.model.valueobjects.OtpId;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.OtpPurpose;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.UserAccountId;

import java.time.Instant;

/**
 * Raised when a one-time code is issued. It carries no code: only its hash is kept by the aggregate.
 */
public record OtpGeneratedEvent(OtpId otpId, UserAccountId userAccountId, OtpPurpose purpose, Instant expiresAt) {
}
