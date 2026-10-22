package com.healthify.guardian.platform.iam.domain.model.aggregates;

import com.healthify.guardian.platform.iam.domain.model.events.OtpGeneratedEvent;
import com.healthify.guardian.platform.iam.domain.model.events.OtpVerifiedEvent;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.HashedSecret;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.OtpPurpose;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.UserAccountId;
import com.healthify.guardian.platform.iam.domain.services.SecretHasher;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OneTimePasswordTest {

    private static final String CODE = "048213";

    // Prefixes the code instead of hashing it: enough to tell a match from a mismatch
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

    private static OneTimePassword issue(OtpPurpose purpose) {
        var expiresAt = Instant.now().plus(Duration.ofMinutes(5));
        return new OneTimePassword(UserAccountId.generate(), purpose, HASHER.hash(CODE), expiresAt);
    }

    @Test
    void issuesAUsableCodeForItsPurpose() {
        var otp = issue(OtpPurpose.EMAIL_VERIFICATION);

        assertThat(otp.getPurpose()).isEqualTo(OtpPurpose.EMAIL_VERIFICATION);
        assertThat(otp.isUsable(Instant.now())).isTrue();
        assertThat(otp.getCodeHash()).isEqualTo(HASHER.hash(CODE));
        assertThat(otp.domainEvents()).singleElement().isInstanceOf(OtpGeneratedEvent.class);
    }

    @Test
    void requiresAPurpose() {
        assertThatThrownBy(() -> issue(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("otp.purpose.invalid");
    }

    @Test
    void rejectsAnExpirationThatIsNotInTheFuture() {
        assertThatThrownBy(() -> new OneTimePassword(UserAccountId.generate(), OtpPurpose.LOGIN, HASHER.hash(CODE),
                Instant.now().minusSeconds(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("otp.expires-at.invalid");
    }

    @Test
    void theRightCodeIsConsumedAndCannotBeUsedAgain() {
        var otp = issue(OtpPurpose.LOGIN);
        otp.clearDomainEvents();
        var now = Instant.now();

        assertThat(otp.verify(CODE, HASHER, now)).isTrue();

        assertThat(otp.isUsed()).isTrue();
        assertThat(otp.getUsedAt()).isEqualTo(now);
        assertThat(otp.domainEvents()).singleElement().isInstanceOf(OtpVerifiedEvent.class);
        assertThat(otp.verify(CODE, HASHER, now)).isFalse();
    }

    @Test
    void aWrongCodeCountsAsAFailedAttempt() {
        var otp = issue(OtpPurpose.LOGIN);

        assertThat(otp.verify("111111", HASHER, Instant.now())).isFalse();
        assertThat(otp.verify(null, HASHER, Instant.now())).isFalse();

        assertThat(otp.getFailedAttempts()).isEqualTo(2);
        assertThat(otp.isUsed()).isFalse();
    }

    @Test
    void locksAfterTooManyWrongCodes() {
        var otp = issue(OtpPurpose.LOGIN);
        for (int i = 0; i < OneTimePassword.MAX_FAILED_ATTEMPTS; i++) {
            otp.verify("111111", HASHER, Instant.now());
        }

        assertThat(otp.isLocked()).isTrue();
        assertThat(otp.verify(CODE, HASHER, Instant.now())).isFalse();
        assertThat(otp.getFailedAttempts()).isEqualTo(OneTimePassword.MAX_FAILED_ATTEMPTS);
    }

    @Test
    void anExpiredCodeIsNeverAccepted() {
        var otp = issue(OtpPurpose.LOGIN);
        var afterExpiration = otp.getExpiresAt();

        assertThat(otp.isExpired(afterExpiration)).isTrue();
        assertThat(otp.verify(CODE, HASHER, afterExpiration)).isFalse();
        assertThat(otp.getFailedAttempts()).isZero();
    }

    @Test
    void cannotBeMarkedAsUsedTwice() {
        var otp = issue(OtpPurpose.LOGIN);
        otp.markAsUsed(Instant.now());

        assertThatThrownBy(() -> otp.markAsUsed(Instant.now()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("otp.already-used");
    }
}
