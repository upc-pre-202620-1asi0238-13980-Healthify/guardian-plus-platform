package com.healthify.guardian.platform.iam.domain.repositories;

import com.healthify.guardian.platform.iam.domain.model.aggregates.PasswordResetToken;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.HashedSecret;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.PasswordResetTokenId;

import java.time.Instant;
import java.util.Optional;

/**
 * Password reset token aggregate repository port.
 */
public interface PasswordResetTokenRepository {

    PasswordResetToken save(PasswordResetToken token);

    Optional<PasswordResetToken> findById(PasswordResetTokenId id);

    /**
     * Retrieves the token with the given hash that is neither used nor expired at {@code now}. Requires a
     * deterministic {@code SecretHasher} (same token, same hash), unlike salted password hashing.
     */
    Optional<PasswordResetToken> findUsableByTokenHash(HashedSecret tokenHash, Instant now);

    /**
     * Deletes the tokens that expired before {@code threshold} without being used.
     *
     * @return how many tokens were deleted
     */
    int deleteExpiredUnusedBefore(Instant threshold);
}
