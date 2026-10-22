package com.healthify.guardian.platform.iam.domain.repositories;

import com.healthify.guardian.platform.iam.domain.model.aggregates.UserAccount;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.Email;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.HashedSecret;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.UserAccountId;

import java.util.Optional;

/**
 * User account aggregate repository port.
 */
public interface UserAccountRepository {

    UserAccount save(UserAccount account);

    Optional<UserAccount> findById(UserAccountId id);

    Optional<UserAccount> findByEmail(Email email);

    /**
     * Tells whether an account already uses the given e-mail, backing the e-mail uniqueness policy.
     */
    boolean existsByEmail(Email email);

    /**
     * Retrieves the account whose pending password reset token has the given hash. Requires a deterministic
     * {@code SecretHasher} (same token, same hash), unlike salted password hashing.
     */
    Optional<UserAccount> findByPasswordResetTokenHash(HashedSecret tokenHash);
}
