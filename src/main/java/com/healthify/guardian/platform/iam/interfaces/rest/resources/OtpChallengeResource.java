package com.healthify.guardian.platform.iam.interfaces.rest.resources;

import java.util.UUID;

/**
 * Response payload of a correct e-mail and password: the login continues with the OTP sent by e-mail.
 */
public record OtpChallengeResource(
        UUID userAccountId,
        String message
) {
}
