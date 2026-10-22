package com.healthify.guardian.platform.iam.domain.model.aggregates;

import com.healthify.guardian.platform.iam.domain.model.commands.RegisterUserCredentialsCommand;
import com.healthify.guardian.platform.iam.domain.model.events.EmailVerifiedEvent;
import com.healthify.guardian.platform.iam.domain.model.events.TwoFactorSettingChangedEvent;
import com.healthify.guardian.platform.iam.domain.model.events.UserCredentialsRegisteredEvent;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.HashedPassword;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.UserAccountStatus;
import com.healthify.guardian.platform.iam.domain.services.PasswordHasher;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserAccountTest {

    // Reverses the password instead of hashing it: enough to tell a match from a mismatch
    private static final PasswordHasher HASHER = new PasswordHasher() {
        @Override
        public HashedPassword hash(String rawPassword) {
            return new HashedPassword(new StringBuilder(rawPassword).reverse().toString());
        }

        @Override
        public boolean matches(String rawPassword, HashedPassword hash) {
            return hash(rawPassword).equals(hash);
        }
    };

    private static UserAccount register() {
        var command = new RegisterUserCredentialsCommand("Ana@Guardian.pe", "secret-123");
        return new UserAccount(command, HASHER.hash(command.rawPassword()));
    }

    @Test
    void registersPendingEmailVerification() {
        var account = register();

        assertThat(account.getId()).isNotNull();
        assertThat(account.getEmail().value()).isEqualTo("ana@guardian.pe");
        assertThat(account.getStatus()).isEqualTo(UserAccountStatus.PENDING_EMAIL_VERIFICATION);
        assertThat(account.isEmailVerified()).isFalse();
        assertThat(account.requiresSecondFactor()).isFalse();
        assertThat(account.canAuthenticate()).isFalse();
        assertThat(account.domainEvents()).singleElement().isInstanceOf(UserCredentialsRegisteredEvent.class);
    }

    @Test
    void rejectsMissingPasswordHash() {
        var command = new RegisterUserCredentialsCommand("ana@guardian.pe", "secret-123");

        assertThatThrownBy(() -> new UserAccount(command, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("user-account.password-hash.invalid");
    }

    @Test
    void emailVerificationActivatesTheAccount() {
        var account = register();
        account.clearDomainEvents();

        account.confirmEmailVerification();

        assertThat(account.isEmailVerified()).isTrue();
        assertThat(account.getStatus()).isEqualTo(UserAccountStatus.ACTIVE);
        assertThat(account.canAuthenticate()).isTrue();
        assertThat(account.domainEvents()).singleElement().isInstanceOf(EmailVerifiedEvent.class);
    }

    @Test
    void rejectsVerifyingTheEmailTwice() {
        var account = register();
        account.confirmEmailVerification();

        assertThatThrownBy(account::confirmEmailVerification)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("user-account.email.already-verified");
    }

    @Test
    void validatesCredentialsAgainstTheStoredHash() {
        var account = register();

        assertThat(account.validateCredentials("secret-123", HASHER)).isTrue();
        assertThat(account.validateCredentials("wrong", HASHER)).isFalse();
        assertThat(account.validateCredentials(null, HASHER)).isFalse();
    }

    @Test
    void changingThePasswordReplacesTheHash() {
        var account = register();

        account.changePassword(HASHER.hash("new-secret"));

        assertThat(account.validateCredentials("new-secret", HASHER)).isTrue();
        assertThat(account.validateCredentials("secret-123", HASHER)).isFalse();
    }

    @Test
    void togglingTwoFactorRegistersAnEventOnlyWhenItChanges() {
        var account = register();
        account.clearDomainEvents();

        account.enableTwoFactor();
        account.enableTwoFactor();

        assertThat(account.requiresSecondFactor()).isTrue();
        assertThat(account.domainEvents()).singleElement()
                .isEqualTo(new TwoFactorSettingChangedEvent(account.getId(), true, account.getUpdatedAt()));

        account.disableTwoFactor();

        assertThat(account.requiresSecondFactor()).isFalse();
        assertThat(account.domainEvents()).hasSize(2);
    }
}
