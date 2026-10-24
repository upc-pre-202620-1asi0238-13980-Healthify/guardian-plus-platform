package com.healthify.guardian.platform.iam.application.internal.commandservices;

import com.healthify.guardian.platform.iam.application.commandservices.AuthenticationCommandService;
import com.healthify.guardian.platform.iam.application.outboundservices.SessionToken;
import com.healthify.guardian.platform.iam.application.outboundservices.SessionTokenService;
import com.healthify.guardian.platform.iam.domain.model.aggregates.OneTimePassword;
import com.healthify.guardian.platform.iam.domain.model.aggregates.UserAccount;
import com.healthify.guardian.platform.iam.domain.model.commands.LoginCommand;
import com.healthify.guardian.platform.iam.domain.model.commands.RegisterUserCredentialsCommand;
import com.healthify.guardian.platform.iam.domain.model.commands.VerifyOtpCommand;
import com.healthify.guardian.platform.iam.domain.model.events.UserLoggedInEvent;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.HashedPassword;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.HashedSecret;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.OtpPurpose;
import com.healthify.guardian.platform.iam.domain.repositories.OneTimePasswordRepository;
import com.healthify.guardian.platform.iam.domain.repositories.UserAccountRepository;
import com.healthify.guardian.platform.iam.domain.services.PasswordHasher;
import com.healthify.guardian.platform.iam.domain.services.SecretHasher;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
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
class AuthenticationCommandServiceImplTest {

    @Mock UserAccountRepository userAccountRepository;
    @Mock OneTimePasswordRepository oneTimePasswordRepository;
    @Mock PasswordHasher passwordHasher;
    @Mock SecretHasher secretHasher;
    @Mock SessionTokenService sessionTokenService;
    @InjectMocks AuthenticationCommandServiceImpl service;

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
    void loginWithTheRightPasswordLogsInTheAccount() {
        var account = account(true);
        when(userAccountRepository.findByEmail(account.getEmail())).thenReturn(Optional.of(account));
        when(passwordHasher.matches("secret-123", account.getPasswordHash())).thenReturn(true);
        var saved = ArgumentCaptor.forClass(UserAccount.class);
        when(userAccountRepository.save(saved.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.handle(new LoginCommand("ana@guardian.pe", "secret-123"));

        assertThat(result.isSuccess()).isTrue();
        assertThat(saved.getValue().domainEvents()).singleElement().isInstanceOf(UserLoggedInEvent.class);
    }

    @Test
    void anUnknownEmailAndAWrongPasswordFailTheSameWay() {
        var account = account(true);
        when(userAccountRepository.findByEmail(any())).thenReturn(Optional.empty());
        var unknownEmail = service.handle(new LoginCommand("nobody@guardian.pe", "secret-123"));

        when(userAccountRepository.findByEmail(account.getEmail())).thenReturn(Optional.of(account));
        when(passwordHasher.matches("wrong", account.getPasswordHash())).thenReturn(false);
        var wrongPassword = service.handle(new LoginCommand("ana@guardian.pe", "wrong"));

        assertThat(errorCode(unknownEmail)).isEqualTo(AuthenticationCommandService.AUTHENTICATION_FAILED);
        assertThat(errorCode(wrongPassword)).isEqualTo(AuthenticationCommandService.AUTHENTICATION_FAILED);
        assertThat(((Result.Failure<?, ?>) unknownEmail).error()).isEqualTo(((Result.Failure<?, ?>) wrongPassword).error());
        verify(userAccountRepository, never()).save(any());
    }

    @Test
    void anUnverifiedAccountCannotLogIn() {
        var account = account(false);
        when(userAccountRepository.findByEmail(account.getEmail())).thenReturn(Optional.of(account));
        when(passwordHasher.matches("secret-123", account.getPasswordHash())).thenReturn(true);

        var result = service.handle(new LoginCommand("ana@guardian.pe", "secret-123"));

        // The id lets the app continue with the e-mail verification; it is only revealed after the right password
        assertThat(errorCode(result)).isEqualTo(AuthenticationCommandService.EMAIL_NOT_VERIFIED);
        assertThat(((ApplicationError) ((Result.Failure<?, ?>) result).error()).details())
                .isEqualTo(account.getId().value().toString());
    }

    @Test
    void theRightOtpAuthenticatesAndIssuesTheSessionToken() {
        var account = account(true);
        var otp = new OneTimePassword(account.getId(), OtpPurpose.LOGIN, new HashedSecret("code-hash"),
                Instant.now().plus(Duration.ofMinutes(5)));
        var token = new SessionToken("jwt", Instant.now().plus(Duration.ofHours(8)));
        when(oneTimePasswordRepository.findLatestUsableByUserAccountIdAndPurpose(eq(account.getId()), eq(OtpPurpose.LOGIN), any()))
                .thenReturn(Optional.of(otp));
        when(secretHasher.matches("048213", otp.getCodeHash())).thenReturn(true);
        when(userAccountRepository.findById(account.getId())).thenReturn(Optional.of(account));
        when(userAccountRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(sessionTokenService.issue(account)).thenReturn(token);

        var result = service.handle(new VerifyOtpCommand(account.getId().value(), "048213"));

        assertThat(result).isEqualTo(Result.success(token));
        assertThat(otp.isUsed()).isTrue();
        verify(oneTimePasswordRepository).save(otp);
    }

    @Test
    void aWrongOtpIsRejectedAndCountedAsAFailedAttempt() {
        var account = account(true);
        var otp = new OneTimePassword(account.getId(), OtpPurpose.LOGIN, new HashedSecret("code-hash"),
                Instant.now().plus(Duration.ofMinutes(5)));
        when(oneTimePasswordRepository.findLatestUsableByUserAccountIdAndPurpose(eq(account.getId()), eq(OtpPurpose.LOGIN), any()))
                .thenReturn(Optional.of(otp));
        when(secretHasher.matches("111111", otp.getCodeHash())).thenReturn(false);

        var result = service.handle(new VerifyOtpCommand(account.getId().value(), "111111"));

        assertThat(errorCode(result)).isEqualTo(AuthenticationCommandService.AUTHENTICATION_FAILED);
        assertThat(otp.getFailedAttempts()).isEqualTo(1);
        verify(oneTimePasswordRepository).save(otp);
        verify(sessionTokenService, never()).issue(any());
    }

    @Test
    void anOtpRequestWithoutAPendingCodeIsRejected() {
        var account = account(true);
        when(oneTimePasswordRepository.findLatestUsableByUserAccountIdAndPurpose(eq(account.getId()), eq(OtpPurpose.LOGIN), any()))
                .thenReturn(Optional.empty());

        var result = service.handle(new VerifyOtpCommand(account.getId().value(), "048213"));

        assertThat(errorCode(result)).isEqualTo(AuthenticationCommandService.AUTHENTICATION_FAILED);
    }
}
