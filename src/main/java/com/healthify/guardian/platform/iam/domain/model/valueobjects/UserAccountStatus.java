package com.healthify.guardian.platform.iam.domain.model.valueobjects;

/**
 * Lifecycle status of a {@code UserAccount}: {@code PENDING_EMAIL_VERIFICATION → ACTIVE}, reached once the
 * e-mail address is verified.
 */
public enum UserAccountStatus {
    PENDING_EMAIL_VERIFICATION,
    ACTIVE;

    /** Tells whether an account in this status may log in. */
    public boolean canAuthenticate() {
        return this == ACTIVE;
    }
}
