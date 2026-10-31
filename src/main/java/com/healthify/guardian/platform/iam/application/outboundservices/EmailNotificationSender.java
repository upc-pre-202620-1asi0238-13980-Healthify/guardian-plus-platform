package com.healthify.guardian.platform.iam.application.outboundservices;

import com.healthify.guardian.platform.iam.domain.model.valueobjects.Email;

import java.time.Instant;

/**
 * Outbound port through which IAM e-mails its secrets to the user. The plain code or token only travels through
 * here: the aggregates keep nothing but its hash.
 */
public interface EmailNotificationSender {

    /**
     * Sends the code that confirms the e-mail address of a new account.
     *
     * @return true if the e-mail provider accepted the message
     */
    boolean sendEmailVerificationCode(Email to, String code, Instant expiresAt);

    /**
     * Sends the second-factor code (OTP) of a login.
     *
     * @return true if the e-mail provider accepted the message
     */
    boolean sendLoginCode(Email to, String code, Instant expiresAt);

    /**
     * Sends the token that allows setting a new password.
     *
     * @return true if the e-mail provider accepted the message
     */
    boolean sendPasswordResetToken(Email to, String token, Instant expiresAt);
}
