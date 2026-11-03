package com.healthify.guardian.platform.iam.domain.services;

import com.healthify.guardian.platform.iam.domain.model.valueobjects.HashedSecret;

/**
 * Domain port that hashes short-lived secrets (OTP codes and password reset tokens) and checks a candidate
 * against a stored hash, so neither is ever kept in plain text.
 */
public interface SecretHasher {

    /**
     * Hashes a plain secret.
     *
     * @param rawSecret the code or token as sent to the user
     * @return its irreversible hash
     */
    HashedSecret hash(String rawSecret);

    /**
     * Tells whether a plain secret corresponds to the given hash.
     *
     * @param rawSecret the code or token as typed by the user
     * @param hash      the stored hash
     * @return true if the secret matches
     */
    boolean matches(String rawSecret, HashedSecret hash);
}
