package com.healthify.guardian.platform.iam.domain.model.aggregates;

import com.healthify.guardian.platform.iam.domain.model.commands.RequestPasswordResetCommand;
import com.healthify.guardian.platform.iam.domain.model.events.PasswordResetCompletedEvent;
import com.healthify.guardian.platform.iam.domain.model.events.PasswordResetTokenIssuedEvent;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.HashedSecret;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.UserAccountId;
import com.healthify.guardian.platform.iam.domain.services.SecretHasher;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordResetTokenTest {

    private static final String TOKEN = "q3Vt9xLwZr0aKp7c";
    private static final RequestPasswordResetCommand COMMAND = new RequestPasswordResetCommand("ana@guardian.pe");

    // Prefixes the token instead of hashing it: enough to tell a match from a mismatch
    private static final SecretHasher HASHER = new SecretHasher() {
        @Override
        public HashedSecret hash(String rawSecret) {
            return new HashedSecret("hashed:" + rawSecret);
        }

        @Override
        public boolean matches(String rawSecret, HashedSecret hash) {
            return hash(rawSecret).equals(hash);
        }
    };

    private static PasswordResetToken issue() {
        return new PasswordResetToken(COMMAND, UserAccountId.generate(), HASHER.hash(TOKEN),
                Instant.now().plus(Duration.ofMinutes(30)));
    }

    @Test
    void issuesAUsableToken() {
        var token = issue();

        assertThat(token.isUsable(Instant.now())).isTrue();
        assertThat(token.matches(TOKEN, HASHER)).isTrue();
        assertThat(token.matches("other", HASHER)).isFalse();
        assertThat(token.matches(null, HASHER)).isFalse();
        assertThat(token.domainEvents()).singleElement().isInstanceOf(PasswordResetTokenIssuedEvent.class);
    }

    @Test
    void requiresTheAccountItBelongsTo() {
        assertThatThrownBy(() -> new PasswordResetToken(COMMAND, null, HASHER.hash(TOKEN), Instant.now().plusSeconds(60)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("user-account.id.invalid");
    }

    @Test
    void rejectsAnExpirationThatIsNotInTheFuture() {
        assertThatThrownBy(() -> new PasswordResetToken(COMMAND, UserAccountId.generate(), HASHER.hash(TOKEN), Instant.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("password-reset-token.expires-at.invalid");
    }

    @Test
    void redeemingConsumesTheToken() {
        var token = issue();
        token.clearDomainEvents();
        var now = Instant.now();

        token.markAsUsed(now);

        assertThat(token.isUsed()).isTrue();
        assertThat(token.isUsable(now)).isFalse();
        assertThat(token.domainEvents()).singleElement().isInstanceOf(PasswordResetCompletedEvent.class);
        assertThatThrownBy(() -> token.markAsUsed(now))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("password-reset-token.already-used");
    }

    @Test
    void anExpiredTokenCannotBeRedeemed() {
        var token = issue();
        var afterExpiration = token.getExpiresAt();

        assertThat(token.isExpired(afterExpiration)).isTrue();
        assertThat(token.isUsable(afterExpiration)).isFalse();
        assertThatThrownBy(() -> token.markAsUsed(afterExpiration))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("password-reset-token.expired");
    }
}
