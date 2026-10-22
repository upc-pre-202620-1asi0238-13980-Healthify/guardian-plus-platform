package com.healthify.guardian.platform.iam.domain.model.aggregates;

import com.healthify.guardian.platform.iam.domain.model.commands.RequestPasswordResetCommand;
import com.healthify.guardian.platform.iam.domain.model.events.PasswordResetCompletedEvent;
import com.healthify.guardian.platform.iam.domain.model.events.PasswordResetTokenIssuedEvent;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.HashedSecret;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.PasswordResetTokenId;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.UserAccountId;
import com.healthify.guardian.platform.iam.domain.services.SecretHasher;
import com.healthify.guardian.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.time.Instant;

/**
 * Aggregate root for an opaque, single-use token sent by e-mail to reset the password of an account. Only the hash
 * of the token is kept. An account may hold several tokens over time, each with its own lifetime.
 */
@Getter
public class PasswordResetToken extends AbstractDomainAggregateRoot<PasswordResetToken> {

    private static final String TOKEN_HASH_INVALID_KEY = "hashed-secret.invalid";
    private static final String USER_ACCOUNT_ID_INVALID_KEY = "user-account.id.invalid";
    private static final String EXPIRES_AT_INVALID_KEY = "password-reset-token.expires-at.invalid";
    private static final String ALREADY_USED_KEY = "password-reset-token.already-used";
    private static final String EXPIRED_KEY = "password-reset-token.expired";

    private PasswordResetTokenId id;
    private UserAccountId userAccountId;
    private HashedSecret tokenHash;
    private Instant expiresAt;
    private Instant usedAt;
    private Instant createdAt;

    /** Reconstitution constructor, used by the persistence assembler. */
    public PasswordResetToken() {
    }

    /**
     * Issues a new token. Registers {@link PasswordResetTokenIssuedEvent}.
     *
     * @param command       the reset request
     * @param userAccountId the account whose e-mail matched {@code command.email()}
     * @param tokenHash     the hash of the generated token, produced by the {@link SecretHasher}
     * @param expiresAt     when the token stops being accepted; must be in the future
     */
    public PasswordResetToken(RequestPasswordResetCommand command, UserAccountId userAccountId,
                              HashedSecret tokenHash, Instant expiresAt) {
        var now = Instant.now();
        if (userAccountId == null) {
            throw new IllegalArgumentException(USER_ACCOUNT_ID_INVALID_KEY);
        }
        if (tokenHash == null) {
            throw new IllegalArgumentException(TOKEN_HASH_INVALID_KEY);
        }
        if (expiresAt == null || !expiresAt.isAfter(now)) {
            throw new IllegalArgumentException(EXPIRES_AT_INVALID_KEY);
        }
        this.id = PasswordResetTokenId.generate();
        this.userAccountId = userAccountId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.createdAt = now;
        registerDomainEvent(new PasswordResetTokenIssuedEvent(id, userAccountId, expiresAt));
    }

    /**
     * Tells whether a plain token corresponds to this one.
     *
     * @param rawToken the token as received from the reset link
     * @param hasher   the hasher that produced {@link #tokenHash}
     * @return true if the token matches
     */
    public boolean matches(String rawToken, SecretHasher hasher) {
        return rawToken != null && hasher.matches(rawToken, tokenHash);
    }

    /**
     * Redeems the token once the new password is set, so it cannot be used again. Registers
     * {@link PasswordResetCompletedEvent}.
     *
     * @param now the current instant
     * @throws IllegalStateException if the token was already used or is expired
     */
    public void markAsUsed(Instant now) {
        if (isUsed()) {
            throw new IllegalStateException(ALREADY_USED_KEY);
        }
        if (isExpired(now)) {
            throw new IllegalStateException(EXPIRED_KEY);
        }
        this.usedAt = now;
        registerDomainEvent(new PasswordResetCompletedEvent(id, userAccountId, now));
    }

    /** Whether the token can still be redeemed at {@code now}. */
    public boolean isUsable(Instant now) {
        return !isUsed() && !isExpired(now);
    }

    /** Whether the token's lifetime is over at {@code now}. */
    public boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }

    /** Whether the token was already redeemed. */
    public boolean isUsed() {
        return usedAt != null;
    }

    /** Restores state from persistence. Used by the persistence assembler. */
    public void setId(PasswordResetTokenId id) {
        this.id = id;
    }

    public void setUserAccountId(UserAccountId userAccountId) {
        this.userAccountId = userAccountId;
    }

    public void setTokenHash(HashedSecret tokenHash) {
        this.tokenHash = tokenHash;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void setUsedAt(Instant usedAt) {
        this.usedAt = usedAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
