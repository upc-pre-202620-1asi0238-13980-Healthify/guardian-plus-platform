package com.healthify.guardian.platform.iam.domain.model.aggregates;

import com.healthify.guardian.platform.iam.domain.model.commands.RegisterUserCredentialsCommand;
import com.healthify.guardian.platform.iam.domain.model.events.EmailVerifiedEvent;
import com.healthify.guardian.platform.iam.domain.model.events.PasswordResetCompletedEvent;
import com.healthify.guardian.platform.iam.domain.model.events.PasswordResetTokenGeneratedEvent;
import com.healthify.guardian.platform.iam.domain.model.events.UserAuthenticatedEvent;
import com.healthify.guardian.platform.iam.domain.model.events.UserCredentialsRegisteredEvent;
import com.healthify.guardian.platform.iam.domain.model.events.UserLoggedInEvent;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.HashedPassword;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.HashedSecret;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.UserAccountStatus;
import com.healthify.guardian.platform.iam.domain.services.PasswordHasher;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

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

    private static UserAccount verified() {
        var account = register();
        account.confirmEmailVerification();
        account.clearDomainEvents();
        return account;
    }

    @Test
    void registersPendingEmailVerification() {
        var account = register();

        assertThat(account.getId()).isNotNull();
        assertThat(account.getEmail().value()).isEqualTo("ana@guardian.pe");
        assertThat(account.getStatus()).isEqualTo(UserAccountStatus.PENDING_EMAIL_VERIFICATION);
        assertThat(account.isEmailVerified()).isFalse();
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
        var account = verified();

        assertThatThrownBy(account::confirmEmailVerification)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("user-account.email.already-verified");
    }

    @Test
    void logsInWithTheRightPassword() {
        var account = verified();

        assertThat(account.logIn("secret-123", HASHER)).isTrue();
        assertThat(account.domainEvents()).singleElement().isInstanceOf(UserLoggedInEvent.class);
    }

    @Test
    void aWrongPasswordDoesNotLogIn() {
        var account = verified();

        assertThat(account.logIn("wrong", HASHER)).isFalse();
        assertThat(account.logIn(null, HASHER)).isFalse();
        assertThat(account.domainEvents()).isEmpty();
    }

    @Test
    void cannotLogInBeforeVerifyingTheEmail() {
        var account = register();

        assertThatThrownBy(() -> account.logIn("secret-123", HASHER))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("user-account.email.not-verified");
    }

    @Test
    void authenticatesAVerifiedAccount() {
        var account = verified();

        account.authenticate();

        assertThat(account.domainEvents()).singleElement().isInstanceOf(UserAuthenticatedEvent.class);
    }

    @Test
    void resetsThePasswordWithAPendingToken() {
        var account = verified();
        account.generatePasswordResetToken(new HashedSecret("token-hash"), Instant.now().plus(Duration.ofMinutes(30)));

        assertThat(account.hasUsablePasswordResetToken(Instant.now())).isTrue();
        assertThat(account.domainEvents()).singleElement().isInstanceOf(PasswordResetTokenGeneratedEvent.class);
        account.clearDomainEvents();

        account.resetPassword(HASHER.hash("new-secret"), Instant.now());

        assertThat(account.logIn("new-secret", HASHER)).isTrue();
        assertThat(account.getPasswordResetTokenHash()).isNull();
        assertThat(account.domainEvents()).first().isInstanceOf(PasswordResetCompletedEvent.class);
    }

    @Test
    void anExpiredResetTokenCannotBeUsed() {
        var account = verified();
        account.generatePasswordResetToken(new HashedSecret("token-hash"), Instant.now().plus(Duration.ofMinutes(30)));
        var afterExpiration = account.getPasswordResetTokenExpiresAt();

        assertThatThrownBy(() -> account.resetPassword(HASHER.hash("new-secret"), afterExpiration))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("password-reset-token.invalid");
    }

    @Test
    void cannotResetThePasswordWithoutAToken() {
        var account = verified();

        assertThatThrownBy(() -> account.resetPassword(HASHER.hash("new-secret"), Instant.now()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("password-reset-token.invalid");
    }

    @Test
    void rejectsAResetTokenThatIsAlreadyExpired() {
        var account = verified();

        assertThatThrownBy(() -> account.generatePasswordResetToken(new HashedSecret("token-hash"), Instant.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("password-reset-token.expires-at.invalid");
    }
}
