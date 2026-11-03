package com.healthify.guardian.platform.iam.domain.services;

import com.healthify.guardian.platform.iam.domain.model.valueobjects.HashedPassword;

/**
 * Domain port that turns a plain-text password into an irreversible hash and checks a candidate against it.
 * Implemented in infrastructure with BCrypt, so the domain never stores nor compares passwords in plain text.
 */
public interface PasswordHasher {

    /**
     * Hashes a plain-text password.
     *
     * @param rawPassword the password as typed by the user
     * @return its irreversible hash
     */
    HashedPassword hash(String rawPassword);

    /**
     * Tells whether a plain-text password corresponds to the given hash.
     *
     * @param rawPassword the password as typed by the user
     * @param hash        the stored hash
     * @return true if the password matches
     */
    boolean matches(String rawPassword, HashedPassword hash);
}
