package com.healthify.guardian.platform.iam.domain.services;

/**
 * Domain port that generates the secrets sent to users. Implemented in infrastructure on a cryptographically
 * secure random generator.
 */
public interface SecretGenerator {

    /**
     * Generates a numeric one-time code, e.g. {@code 048213}.
     *
     * @param digits how many digits the code has; leading zeros are kept
     * @return the code
     */
    String generateNumericCode(int digits);

    /**
     * Generates an opaque, URL-safe token for password reset links.
     *
     * @return the token
     */
    String generateOpaqueToken();
}
