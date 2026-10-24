package com.healthify.guardian.platform.iam.domain.model.aggregates;

import com.healthify.guardian.platform.iam.domain.model.commands.RegisterUserCredentialsCommand;
import com.healthify.guardian.platform.iam.domain.model.events.EmailVerifiedEvent;
import com.healthify.guardian.platform.iam.domain.model.events.PasswordResetCompletedEvent;
import com.healthify.guardian.platform.iam.domain.model.events.PasswordResetTokenGeneratedEvent;
import com.healthify.guardian.platform.iam.domain.model.events.UserAuthenticatedEvent;
import com.healthify.guardian.platform.iam.domain.model.events.UserCredentialsRegisteredEvent;
import com.healthify.guardian.platform.iam.domain.model.events.UserLoggedInEvent;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.Email;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.HashedPassword;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.HashedSecret;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.UserAccountId;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.UserAccountStatus;
import com.healthify.guardian.platform.iam.domain.services.PasswordHasher;
import com.healthify.guardian.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.time.Instant;

/**
 * Aggregate root holding the digital identity of a registered caregiver or family member. The e-mail is the only
 * login identifier, and the account cannot log in until that e-mail is verified
 * ({@code PENDING_EMAIL_VERIFICATION → ACTIVE}).
 *
 * <p>Every login needs two steps: the right password ({@link #logIn}) and then the right OTP sent by e-mail
 * ({@link #authenticate}). The account also owns its pending password reset token, kept only as a hash.</p>
 */
@Getter
public class UserAccount extends AbstractDomainAggregateRoot<UserAccount> {

    private static final String PASSWORD_HASH_INVALID_KEY = "user-account.password-hash.invalid";
    private static final String EMAIL_ALREADY_VERIFIED_KEY = "user-account.email.already-verified";
    private static final String CANNOT_AUTHENTICATE_KEY = "user-account.email.not-verified";
    private static final String RESET_TOKEN_HASH_INVALID_KEY = "hashed-secret.invalid";
    private static final String RESET_TOKEN_EXPIRES_AT_INVALID_KEY = "password-reset-token.expires-at.invalid";
    private static final String RESET_TOKEN_NOT_USABLE_KEY = "password-reset-token.invalid";

    private UserAccountId id;
    private Email email;
    private HashedPassword passwordHash;
    private boolean emailVerified;
    private UserAccountStatus status;
    private HashedSecret passwordResetTokenHash;
    private Instant passwordResetTokenExpiresAt;
    private Instant createdAt;
    private Instant updatedAt;

    /** Reconstitution constructor, used by the persistence assembler. */
    public UserAccount() {
    }

    /**
     * Registers new credentials, pending e-mail verification. Registers {@link UserCredentialsRegisteredEvent}.
     *
     * @param command      the registration data
     * @param passwordHash the hash of {@code command.rawPassword()}, produced by the {@link PasswordHasher}
     */
    public UserAccount(RegisterUserCredentialsCommand command, HashedPassword passwordHash) {
        if (passwordHash == null) {
            throw new IllegalArgumentException(PASSWORD_HASH_INVALID_KEY);
        }
        var now = Instant.now();
        this.id = UserAccountId.generate();
        this.email = new Email(command.email());
        this.passwordHash = passwordHash;
        this.emailVerified = false;
        this.status = UserAccountStatus.PENDING_EMAIL_VERIFICATION;
        this.createdAt = now;
        this.updatedAt = now;
        registerDomainEvent(new UserCredentialsRegisteredEvent(id, email, now));
    }

    /**
     * Marks the e-mail as verified and activates the account. Registers {@link EmailVerifiedEvent}.
     *
     * @throws IllegalStateException if the e-mail was already verified
     */
    public void confirmEmailVerification() {
        if (emailVerified) {
            throw new IllegalStateException(EMAIL_ALREADY_VERIFIED_KEY);
        }
        var now = Instant.now();
        this.emailVerified = true;
        this.status = UserAccountStatus.ACTIVE;
        this.updatedAt = now;
        registerDomainEvent(new EmailVerifiedEvent(id, email, now));
    }

    /**
     * First step of a login: checks the password. On a match registers {@link UserLoggedInEvent}, which triggers
     * the second factor.
     *
     * @param rawPassword the password as typed by the user
     * @param hasher      the hasher that produced {@link #passwordHash}
     * @return true if the password matches
     * @throws IllegalStateException if the e-mail is not verified yet
     */
    public boolean logIn(String rawPassword, PasswordHasher hasher) {
        if (rawPassword == null || !hasher.matches(rawPassword, passwordHash)) {
            return false;
        }
        if (!canAuthenticate()) {
            throw new IllegalStateException(CANNOT_AUTHENTICATE_KEY);
        }
        registerDomainEvent(new UserLoggedInEvent(id, Instant.now()));
        return true;
    }

    /**
     * Last step of a login, once the OTP was verified. Registers {@link UserAuthenticatedEvent}.
     *
     * @throws IllegalStateException if the e-mail is not verified
     */
    public void authenticate() {
        if (!canAuthenticate()) {
            throw new IllegalStateException(CANNOT_AUTHENTICATE_KEY);
        }
        registerDomainEvent(new UserAuthenticatedEvent(id, Instant.now()));
    }

    /**
     * Stores a new password reset token, replacing any previous one. Registers
     * {@link PasswordResetTokenGeneratedEvent}.
     *
     * @param tokenHash the hash of the generated token, produced by the {@code SecretHasher}
     * @param expiresAt when the token stops being accepted; must be in the future
     */
    public void generatePasswordResetToken(HashedSecret tokenHash, Instant expiresAt) {
        var now = Instant.now();
        if (tokenHash == null) {
            throw new IllegalArgumentException(RESET_TOKEN_HASH_INVALID_KEY);
        }
        if (expiresAt == null || !expiresAt.isAfter(now)) {
            throw new IllegalArgumentException(RESET_TOKEN_EXPIRES_AT_INVALID_KEY);
        }
        this.passwordResetTokenHash = tokenHash;
        this.passwordResetTokenExpiresAt = expiresAt;
        this.updatedAt = now;
        registerDomainEvent(new PasswordResetTokenGeneratedEvent(id, email, expiresAt));
    }

    /**
     * Sets a new password with the pending reset token, which is consumed. Registers
     * {@link PasswordResetCompletedEvent}.
     *
     * @param newPasswordHash the hash of the new password
     * @param now             the current instant
     * @throws IllegalStateException if there is no reset token or it expired
     */
    public void resetPassword(HashedPassword newPasswordHash, Instant now) {
        if (newPasswordHash == null) {
            throw new IllegalArgumentException(PASSWORD_HASH_INVALID_KEY);
        }
        if (!hasUsablePasswordResetToken(now)) {
            throw new IllegalStateException(RESET_TOKEN_NOT_USABLE_KEY);
        }
        this.passwordHash = newPasswordHash;
        this.passwordResetTokenHash = null;
        this.passwordResetTokenExpiresAt = null;
        this.updatedAt = now;
        registerDomainEvent(new PasswordResetCompletedEvent(id, now));
    }

    /** Whether a password reset token is pending and still valid at {@code now}. */
    public boolean hasUsablePasswordResetToken(Instant now) {
        return passwordResetTokenHash != null && now.isBefore(passwordResetTokenExpiresAt);
    }

    /** Whether the account may log in, i.e. its e-mail is verified. */
    public boolean canAuthenticate() {
        return status.canAuthenticate();
    }

    /** Restores state from persistence. Used by the persistence assembler. */
    public void setId(UserAccountId id) {
        this.id = id;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    public void setPasswordHash(HashedPassword passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public void setStatus(UserAccountStatus status) {
        this.status = status;
    }

    public void setPasswordResetTokenHash(HashedSecret passwordResetTokenHash) {
        this.passwordResetTokenHash = passwordResetTokenHash;
    }

    public void setPasswordResetTokenExpiresAt(Instant passwordResetTokenExpiresAt) {
        this.passwordResetTokenExpiresAt = passwordResetTokenExpiresAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
