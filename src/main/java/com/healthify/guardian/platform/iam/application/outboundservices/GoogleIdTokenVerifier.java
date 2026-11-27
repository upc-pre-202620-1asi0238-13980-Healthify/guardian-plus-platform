package com.healthify.guardian.platform.iam.application.outboundservices;

import java.util.Optional;

/**
 * Outbound port that checks the ID tokens Google issues to the mobile app.
 */
public interface GoogleIdTokenVerifier {

    /**
     * Checks the signature, issuer, audience and expiration of a Google ID token.
     *
     * @param idToken the token the app got from Google
     * @return the Google account it belongs to, or empty if the token is invalid or its e-mail is not verified
     */
    Optional<GoogleIdentity> verify(String idToken);
}
