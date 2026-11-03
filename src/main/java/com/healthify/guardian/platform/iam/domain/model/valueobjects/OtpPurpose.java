package com.healthify.guardian.platform.iam.domain.model.valueobjects;

/**
 * What a one-time code was issued for. A code issued for one purpose is never accepted for the other.
 */
public enum OtpPurpose {
    /** Second factor of a login attempt. */
    LOGIN,
    /** Confirmation of the e-mail address right after registration. */
    EMAIL_VERIFICATION
}
