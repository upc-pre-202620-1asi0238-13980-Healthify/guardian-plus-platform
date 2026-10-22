package com.healthify.guardian.platform.iam.domain.model.aggregates;

import com.healthify.guardian.platform.iam.domain.model.commands.GenerateOtpCommand;
import com.healthify.guardian.platform.iam.domain.model.events.OtpGeneratedEvent;
import com.healthify.guardian.platform.iam.domain.model.events.OtpVerifiedEvent;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.HashedSecret;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.OtpId;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.OtpPurpose;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.UserAccountId;
import com.healthify.guardian.platform.iam.domain.services.SecretHasher;
import com.healthify.guardian.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.time.Instant;
import java.util.Locale;

/**
 * Aggregate root for a one-time code sent to the user's e-mail, either as the second factor of a login or to
 * verify the e-mail after registration. Only the hash of the code is kept.
 *
 * <p>Its state is not stored as a column: a code is usable only while it is not used, not expired and has not
 * reached {@link #MAX_FAILED_ATTEMPTS} wrong guesses.</p>
 */
@Getter
public class OneTimePassword extends AbstractDomainAggregateRoot<OneTimePassword> {

    /** Wrong guesses allowed before the code is locked, so a 6-digit code cannot be brute-forced. */
    public static final int MAX_FAILED_ATTEMPTS = 5;

    private static final String CODE_HASH_INVALID_KEY = "hashed-secret.invalid";
    private static final String PURPOSE_INVALID_KEY = "otp.purpose.invalid";
    private static final String EXPIRES_AT_INVALID_KEY = "otp.expires-at.invalid";
    private static final String ALREADY_USED_KEY = "otp.already-used";

    private OtpId id;
    private UserAccountId userAccountId;
    private HashedSecret codeHash;
    private OtpPurpose purpose;
    private Instant expiresAt;
    private Instant usedAt;
    private int failedAttempts;
    private Instant createdAt;

    /** Reconstitution constructor, used by the persistence assembler. */
    public OneTimePassword() {
    }

    /**
     * Issues a new code. Registers {@link OtpGeneratedEvent}.
     *
     * @param command   the account and purpose of the code
     * @param codeHash  the hash of the generated code, produced by the {@link SecretHasher}
     * @param expiresAt when the code stops being accepted; must be in the future
     */
    public OneTimePassword(GenerateOtpCommand command, HashedSecret codeHash, Instant expiresAt) {
        var now = Instant.now();
        if (codeHash == null) {
            throw new IllegalArgumentException(CODE_HASH_INVALID_KEY);
        }
        if (expiresAt == null || !expiresAt.isAfter(now)) {
            throw new IllegalArgumentException(EXPIRES_AT_INVALID_KEY);
        }
        this.id = OtpId.generate();
        this.userAccountId = new UserAccountId(command.userAccountId());
        this.codeHash = codeHash;
        this.purpose = parsePurpose(command.purpose());
        this.expiresAt = expiresAt;
        this.failedAttempts = 0;
        this.createdAt = now;
        registerDomainEvent(new OtpGeneratedEvent(id, userAccountId, purpose, expiresAt));
    }

    /**
     * Checks a typed code. A match consumes the code and registers {@link OtpVerifiedEvent}; a mismatch counts
     * as a failed attempt. A code that is no longer usable never matches.
     *
     * @param candidateCode the code as typed by the user
     * @param hasher        the hasher that produced {@link #codeHash}
     * @param now           the current instant
     * @return true if the code matched and was consumed
     */
    public boolean verify(String candidateCode, SecretHasher hasher, Instant now) {
        if (!isUsable(now)) {
            return false;
        }
        if (candidateCode == null || !hasher.matches(candidateCode, codeHash)) {
            failedAttempts++;
            return false;
        }
        markAsUsed(now);
        registerDomainEvent(new OtpVerifiedEvent(id, userAccountId, purpose, now));
        return true;
    }

    /**
     * Consumes the code so it cannot be accepted again.
     *
     * @param now the current instant
     * @throws IllegalStateException if the code was already used
     */
    public void markAsUsed(Instant now) {
        if (isUsed()) {
            throw new IllegalStateException(ALREADY_USED_KEY);
        }
        this.usedAt = now;
    }

    /** Whether the code can still be accepted at {@code now}. */
    public boolean isUsable(Instant now) {
        return !isUsed() && !isExpired(now) && !isLocked();
    }

    /** Whether the code's lifetime is over at {@code now}. */
    public boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }

    /** Whether the code was already consumed. */
    public boolean isUsed() {
        return usedAt != null;
    }

    /** Whether the code reached the maximum number of wrong guesses. */
    public boolean isLocked() {
        return failedAttempts >= MAX_FAILED_ATTEMPTS;
    }

    private static OtpPurpose parsePurpose(String purpose) {
        if (purpose == null) {
            throw new IllegalArgumentException(PURPOSE_INVALID_KEY);
        }
        try {
            return OtpPurpose.valueOf(purpose.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(PURPOSE_INVALID_KEY);
        }
    }

    /** Restores state from persistence. Used by the persistence assembler. */
    public void setId(OtpId id) {
        this.id = id;
    }

    public void setUserAccountId(UserAccountId userAccountId) {
        this.userAccountId = userAccountId;
    }

    public void setCodeHash(HashedSecret codeHash) {
        this.codeHash = codeHash;
    }

    public void setPurpose(OtpPurpose purpose) {
        this.purpose = purpose;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void setUsedAt(Instant usedAt) {
        this.usedAt = usedAt;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
