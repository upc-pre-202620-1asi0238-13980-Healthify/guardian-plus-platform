package com.healthify.guardian.platform.iam.application.internal.commandservices;

import com.healthify.guardian.platform.iam.application.outboundservices.EmailNotificationSender;
import com.healthify.guardian.platform.iam.domain.model.aggregates.OneTimePassword;
import com.healthify.guardian.platform.iam.domain.model.aggregates.UserAccount;
import com.healthify.guardian.platform.iam.domain.model.commands.RegisterUserCredentialsCommand;
import com.healthify.guardian.platform.iam.domain.model.commands.RequestPasswordResetCommand;
import com.healthify.guardian.platform.iam.domain.model.commands.ResetPasswordCommand;
import com.healthify.guardian.platform.iam.domain.model.commands.VerifyEmailCommand;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.HashedPassword;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.HashedSecret;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.OtpPurpose;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.UserAccountStatus;
import com.healthify.guardian.platform.iam.domain.repositories.OneTimePasswordRepository;
import com.healthify.guardian.platform.iam.domain.repositories.UserAccountRepository;
import com.healthify.guardian.platform.iam.domain.services.PasswordHasher;
import com.healthify.guardian.platform.iam.domain.services.SecretGenerator;
import com.healthify.guardian.platform.iam.domain.services.SecretHasher;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAccountCommandServiceImplTest {

    @Mock UserAccountRepository userAccountRepository;
    @Mock OneTimePasswordRepository oneTimePasswordRepository;
    @Mock PasswordHasher passwordHasher;
    @Mock SecretHasher secretHasher;
    @Mock SecretGenerator secretGenerator;
    @Mock EmailNotificationSender emailNotificationSender;
    UserAccountCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserAccountCommandServiceImpl(userAccountRepository, oneTimePasswordRepository, passwordHasher,
                secretHasher, secretGenerator, emailNotificationSender, 30);
    }

    private static UserAccount account(boolean verified) {
        var account = new UserAccount(new RegisterUserCredentialsCommand("ana@guardian.pe", "secret-123"),
                new HashedPassword("hash"));
        if (verified) {
            account.confirmEmailVerification();
        }
        account.clearDomainEvents();
        return account;
    }

    private static String errorCode(Result<?, ?> result) {
        return ((ApplicationError) ((Result.Failure<?, ?>) result).error()).code();
    }

    @Test
    void registersAnAvailableEmail() {
        when(userAccountRepository.existsByEmail(any())).thenReturn(false);
        when(passwordHasher.hash("secret-123")).thenReturn(new HashedPassword("hash"));
        when(userAccountRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.handle(new RegisterUserCredentialsCommand("Ana@Guardian.pe", "secret-123"));

        var account = (UserAccount) ((Result.Success<?, ?>) result).value();
        assertThat(account.getEmail().value()).isEqualTo("ana@guardian.pe");
        assertThat(account.getStatus()).isEqualTo(UserAccountStatus.PENDING_EMAIL_VERIFICATION);
    }

    @Test
    void rejectsATakenEmailWithoutHashingThePassword() {
        when(userAccountRepository.existsByEmail(any())).thenReturn(true);

        var result = service.handle(new RegisterUserCredentialsCommand("ana@guardian.pe", "secret-123"));

        assertThat(errorCode(result)).isEqualTo("USERACCOUNT_CONFLICT");
        verify(passwordHasher, never()).hash(any());
    }

    @Test
    void rejectsAnInvalidEmailAsValidationError() {
        var result = service.handle(new RegisterUserCredentialsCommand("not-an-email", "secret-123"));

        assertThat(errorCode(result)).isEqualTo("VALIDATION_ERROR");
    }

    @Test
    void theRightCodeVerifiesTheEmail() {
        var account = account(false);
        var otp = new OneTimePassword(account.getId(), OtpPurpose.EMAIL_VERIFICATION, new HashedSecret("code-hash"),
                Instant.now().plus(Duration.ofMinutes(5)));
        when(userAccountRepository.findById(account.getId())).thenReturn(Optional.of(account));
        when(oneTimePasswordRepository.findLatestUsableByUserAccountIdAndPurpose(
                eq(account.getId()), eq(OtpPurpose.EMAIL_VERIFICATION), any())).thenReturn(Optional.of(otp));
        when(secretHasher.matches("048213", otp.getCodeHash())).thenReturn(true);
        when(userAccountRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.handle(new VerifyEmailCommand(account.getId().value(), "048213"));

        assertThat(((UserAccount) ((Result.Success<?, ?>) result).value()).getStatus()).isEqualTo(UserAccountStatus.ACTIVE);
        assertThat(otp.isUsed()).isTrue();
    }

    @Test
    void aWrongCodeDoesNotVerifyTheEmail() {
        var account = account(false);
        var otp = new OneTimePassword(account.getId(), OtpPurpose.EMAIL_VERIFICATION, new HashedSecret("code-hash"),
                Instant.now().plus(Duration.ofMinutes(5)));
        when(userAccountRepository.findById(account.getId())).thenReturn(Optional.of(account));
        when(oneTimePasswordRepository.findLatestUsableByUserAccountIdAndPurpose(
                eq(account.getId()), eq(OtpPurpose.EMAIL_VERIFICATION), any())).thenReturn(Optional.of(otp));
        when(secretHasher.matches("111111", otp.getCodeHash())).thenReturn(false);

        var result = service.handle(new VerifyEmailCommand(account.getId().value(), "111111"));

        assertThat(errorCode(result)).isEqualTo("VALIDATION_ERROR");
        assertThat(account.isEmailVerified()).isFalse();
        verify(oneTimePasswordRepository).save(otp);
    }

    @Test
    void anUnknownEmailCannotRequestAPasswordReset() {
        when(userAccountRepository.findByEmail(any())).thenReturn(Optional.empty());

        var result = service.handle(new RequestPasswordResetCommand("nobody@guardian.pe"));

        assertThat(errorCode(result)).isEqualTo("USERACCOUNT_NOT_FOUND");
        verify(emailNotificationSender, never()).sendPasswordResetToken(any(), any(), any());
    }

    @Test
    void aPasswordResetStoresTheTokenHashAndEmailsThePlainToken() {
        var account = account(true);
        when(userAccountRepository.findByEmail(account.getEmail())).thenReturn(Optional.of(account));
        when(secretGenerator.generateOpaqueToken()).thenReturn("opaque-token");
        when(secretHasher.hash("opaque-token")).thenReturn(new HashedSecret("token-hash"));
        when(userAccountRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(emailNotificationSender.sendPasswordResetToken(eq(account.getEmail()), eq("opaque-token"), any())).thenReturn(true);

        var result = service.handle(new RequestPasswordResetCommand("ana@guardian.pe"));

        assertThat(result.isSuccess()).isTrue();
        assertThat(account.getPasswordResetTokenHash()).isEqualTo(new HashedSecret("token-hash"));
    }

    @Test
    void resetsThePasswordWithAValidToken() {
        var account = account(true);
        account.generatePasswordResetToken(new HashedSecret("token-hash"), Instant.now().plus(Duration.ofMinutes(30)));
        when(secretHasher.hash("opaque-token")).thenReturn(new HashedSecret("token-hash"));
        when(userAccountRepository.findByPasswordResetTokenHash(new HashedSecret("token-hash"))).thenReturn(Optional.of(account));
        when(passwordHasher.hash("new-secret-123")).thenReturn(new HashedPassword("new-hash"));
        when(userAccountRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.handle(new ResetPasswordCommand("opaque-token", "new-secret-123"));

        assertThat(result.isSuccess()).isTrue();
        assertThat(account.getPasswordHash()).isEqualTo(new HashedPassword("new-hash"));
        assertThat(account.getPasswordResetTokenHash()).isNull();
    }

    @Test
    void anUnknownResetTokenIsRejected() {
        when(secretHasher.hash("forged")).thenReturn(new HashedSecret("forged-hash"));
        when(userAccountRepository.findByPasswordResetTokenHash(any())).thenReturn(Optional.empty());

        var result = service.handle(new ResetPasswordCommand("forged", "new-secret-123"));

        assertThat(errorCode(result)).isEqualTo("VALIDATION_ERROR");
    }
}
