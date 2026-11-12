package com.healthify.guardian.platform.iam.application.outboundservices;

/**
 * Who a valid Google ID token belongs to.
 *
 * @param subject the stable id of the Google account ({@code sub})
 * @param email   its e-mail, already verified by Google
 */
public record GoogleIdentity(String subject, String email) {
}
