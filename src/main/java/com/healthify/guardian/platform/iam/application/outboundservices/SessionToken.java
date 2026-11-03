package com.healthify.guardian.platform.iam.application.outboundservices;

import java.time.Instant;

/**
 * Signed session token issued when a user is authenticated.
 *
 * @param accessToken the signed JWT, sent by the app as {@code Authorization: Bearer <accessToken>}
 * @param expiresAt   when the token stops being accepted
 */
public record SessionToken(String accessToken, Instant expiresAt) {
}
