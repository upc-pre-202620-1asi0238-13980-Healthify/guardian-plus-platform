package com.healthify.guardian.platform.iam.domain.model.aggregates;

import com.healthify.guardian.platform.iam.domain.model.commands.RegisterUserCredentialsCommand;
import com.healthify.guardian.platform.iam.domain.model.events.EmailVerifiedEvent;
import com.healthify.guardian.platform.iam.domain.model.events.TwoFactorSettingChangedEvent;
import com.healthify.guardian.platform.iam.domain.model.events.UserCredentialsRegisteredEvent;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.Email;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.HashedPassword;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.UserAccountId;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.UserAccountStatus;
import com.healthify.guardian.platform.iam.domain.services.PasswordHasher;
import com.healthify.guardian.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.time.Instant;

/**
 * Aggregate root holding the digital identity of a registered caregiver or family member. The e-mail is the only
 * login identifier, and the account cannot authenticate until that e-mail is verified
 * ({@code PENDING_EMAIL_VERIFICATION → ACTIVE}).
 */
@Getter
public class UserAccount extends AbstractDomainAggregateRoot<UserAccount> {

    private static final String PASSWORD_HASH_INVALID_KEY = "user-account.password-hash.invalid";
    private static final String EMAIL_ALREADY_VERIFIED_KEY = "user-account.email.already-verified";

    private UserAccountId id;
    private Email email;
    private HashedPassword passwordHash;
    private boolean emailVerified;
    private boolean twoFactorEnabled;
    private UserAccountStatus status;
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
        this.twoFactorEnabled = false;
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
     * Tells whether the given plain-text password belongs to this account.
     *
     * @param rawPassword the password as typed by the user
     * @param hasher      the hasher that produced {@link #passwordHash}
     * @return true if the password matches
     */
    public boolean validateCredentials(String rawPassword, PasswordHasher hasher) {
        return rawPassword != null && hasher.matches(rawPassword, passwordHash);
    }

    /**
     * Replaces the password, e.g. after a password reset.
     *
     * @param newPasswordHash the hash of the new password
     */
    public void changePassword(HashedPassword newPasswordHash) {
        if (newPasswordHash == null) {
            throw new IllegalArgumentException(PASSWORD_HASH_INVALID_KEY);
        }
        this.passwordHash = newPasswordHash;
        this.updatedAt = Instant.now();
    }

    /** Requires an OTP sent by e-mail on every login. Registers {@link TwoFactorSettingChangedEvent} if it changed. */
    public void enableTwoFactor() {
        changeTwoFactor(true);
    }

    /** Stops requiring the OTP on login. Registers {@link TwoFactorSettingChangedEvent} if it changed. */
    public void disableTwoFactor() {
        changeTwoFactor(false);
    }

    /** Whether a login must be completed with an OTP after the password is validated. */
    public boolean requiresSecondFactor() {
        return twoFactorEnabled;
    }

    /** Whether the account may log in, i.e. its e-mail is verified. */
    public boolean canAuthenticate() {
        return status.canAuthenticate();
    }

    private void changeTwoFactor(boolean enabled) {
        if (twoFactorEnabled == enabled) {
            return;
        }
        var now = Instant.now();
        this.twoFactorEnabled = enabled;
        this.updatedAt = now;
        registerDomainEvent(new TwoFactorSettingChangedEvent(id, enabled, now));
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

    public void setTwoFactorEnabled(boolean twoFactorEnabled) {
        this.twoFactorEnabled = twoFactorEnabled;
    }

    public void setStatus(UserAccountStatus status) {
        this.status = status;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
