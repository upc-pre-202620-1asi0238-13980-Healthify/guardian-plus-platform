package com.healthify.guardian.platform.iam.domain.model.events;

import com.healthify.guardian.platform.iam.domain.model.valueobjects.OtpId;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.OtpPurpose;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.UserAccountId;

import java.time.Instant;

/**
 * Raised when the typed code matches a usable one-time code, which is consumed at that moment.
 */
public record OtpVerifiedEvent(OtpId otpId, UserAccountId userAccountId, OtpPurpose purpose, Instant verifiedAt) {
}
