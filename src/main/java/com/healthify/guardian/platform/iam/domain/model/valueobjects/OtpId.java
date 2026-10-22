package com.healthify.guardian.platform.iam.domain.model.valueobjects;

import java.util.UUID;

/**
 * Immutable identifier of a {@code OneTimePassword} aggregate.
 *
 * @param value the underlying UUID
 */
public record OtpId(UUID value) {

    private static final String INVALID_MESSAGE_KEY = "otp.id.invalid";

    public OtpId {
        if (value == null) {
            throw new IllegalArgumentException(INVALID_MESSAGE_KEY);
        }
    }

    public static OtpId generate() {
        return new OtpId(UUID.randomUUID());
    }
}
