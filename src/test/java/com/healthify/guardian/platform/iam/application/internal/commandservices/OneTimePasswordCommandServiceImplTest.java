package com.healthify.guardian.platform.iam.application.internal.commandservices;

import com.healthify.guardian.platform.iam.application.outboundservices.EmailNotificationSender;
import com.healthify.guardian.platform.iam.domain.model.aggregates.OneTimePassword;
import com.healthify.guardian.platform.iam.domain.model.aggregates.UserAccount;
import com.healthify.guardian.platform.iam.domain.model.commands.RegisterUserCredentialsCommand;
import com.healthify.guardian.platform.iam.domain.model.commands.SendEmailVerificationRequestCommand;
import com.healthify.guardian.platform.iam.domain.model.commands.TriggerTwoFactorCommand;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.HashedPassword;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.HashedSecret;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.OtpPurpose;
import com.healthify.guardian.platform.iam.domain.repositories.OneTimePasswordRepository;
import com.healthify.guardian.platform.iam.domain.repositories.UserAccountRepository;
import com.healthify.guardian.platform.iam.domain.services.SecretGenerator;
import com.healthify.guardian.platform.iam.domain.services.SecretHasher;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OneTimePasswordCommandServiceImplTest {

    @Mock UserAccountRepository userAccountRepository;
    @Mock OneTimePasswordRepository oneTimePasswordRepository;
    @Mock SecretGenerator secretGenerator;
    @Mock SecretHasher secretHasher;
    @Mock EmailNotificationSender emailNotificationSender;
    OneTimePasswordCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new OneTimePasswordCommandServiceImpl(userAccountRepository, oneTimePasswordRepository,
                secretGenerator, secretHasher, emailNotificationSender, 6, 10);
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

    private void generatesCode() {
        when(secretGenerator.generateNumericCode(6)).thenReturn("048213");
        when(secretHasher.hash("048213")).thenReturn(new HashedSecret("code-hash"));
        when(oneTimePasswordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void triggeringTwoFactorStoresTheHashAndEmailsThePlainCode() {
        var account = account(true);
        when(userAccountRepository.findById(account.getId())).thenReturn(Optional.of(account));
        generatesCode();
        when(emailNotificationSender.sendLoginCode(eq(account.getEmail()), eq("048213"), any())).thenReturn(true);

        var result = service.handle(new TriggerTwoFactorCommand(account.getId().value()));

        var otp = (OneTimePassword) ((Result.Success<?, ?>) result).value();
        assertThat(otp.getPurpose()).isEqualTo(OtpPurpose.LOGIN);
        assertThat(otp.getCodeHash()).isEqualTo(new HashedSecret("code-hash"));
    }

    @Test
    void sendsTheEmailVerificationCodeToAPendingAccount() {
        var account = account(false);
        when(userAccountRepository.findById(account.getId())).thenReturn(Optional.of(account));
        generatesCode();
        when(emailNotificationSender.sendEmailVerificationCode(eq(account.getEmail()), eq("048213"), any())).thenReturn(true);

        var result = service.handle(new SendEmailVerificationRequestCommand(account.getId().value()));

        assertThat(((OneTimePassword) ((Result.Success<?, ?>) result).value()).getPurpose())
                .isEqualTo(OtpPurpose.EMAIL_VERIFICATION);
    }

    @Test
    void doesNotSendAVerificationCodeToAVerifiedAccount() {
        var account = account(true);
        when(userAccountRepository.findById(account.getId())).thenReturn(Optional.of(account));

        var result = service.handle(new SendEmailVerificationRequestCommand(account.getId().value()));

        assertThat(((ApplicationError) ((Result.Failure<?, ?>) result).error()).code()).isEqualTo("BUSINESS_RULE_VIOLATION");
        verify(oneTimePasswordRepository, never()).save(any());
    }

    @Test
    void reportsAnEmailThatCouldNotBeSent() {
        var account = account(true);
        when(userAccountRepository.findById(account.getId())).thenReturn(Optional.of(account));
        generatesCode();
        when(emailNotificationSender.sendLoginCode(any(), any(), any())).thenReturn(false);

        var result = service.handle(new TriggerTwoFactorCommand(account.getId().value()));

        assertThat(((ApplicationError) ((Result.Failure<?, ?>) result).error()).code()).isEqualTo("UNEXPECTED_ERROR");
    }
}
