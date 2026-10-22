package com.healthify.guardian.platform.iam.application.internal.commandservices;

import com.healthify.guardian.platform.iam.application.commandservices.OneTimePasswordCommandService;
import com.healthify.guardian.platform.iam.application.outboundservices.EmailNotificationSender;
import com.healthify.guardian.platform.iam.domain.model.aggregates.OneTimePassword;
import com.healthify.guardian.platform.iam.domain.model.aggregates.UserAccount;
import com.healthify.guardian.platform.iam.domain.model.commands.SendEmailVerificationRequestCommand;
import com.healthify.guardian.platform.iam.domain.model.commands.TriggerTwoFactorCommand;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.OtpPurpose;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.UserAccountId;
import com.healthify.guardian.platform.iam.domain.repositories.OneTimePasswordRepository;
import com.healthify.guardian.platform.iam.domain.repositories.UserAccountRepository;
import com.healthify.guardian.platform.iam.domain.services.SecretGenerator;
import com.healthify.guardian.platform.iam.domain.services.SecretHasher;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;
import com.healthify.guardian.platform.shared.infrastructure.i18n.MessageResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Application service that issues one-time codes: generates the code, keeps only its hash in a
 * {@link OneTimePassword} and e-mails the plain code, which is never stored.
 */
@Service
public class OneTimePasswordCommandServiceImpl implements OneTimePasswordCommandService {

    private static final String EMAIL_ALREADY_VERIFIED_KEY = "user-account.email.already-verified";
    private static final String EMAIL_NOT_VERIFIED_KEY = "user-account.email.not-verified";
    private static final String DELIVERY_FAILED_KEY = "iam.email.delivery-failed";

    private final UserAccountRepository userAccountRepository;
    private final OneTimePasswordRepository oneTimePasswordRepository;
    private final SecretGenerator secretGenerator;
    private final SecretHasher secretHasher;
    private final EmailNotificationSender emailNotificationSender;
    private final int codeDigits;
    private final Duration codeLifetime;

    public OneTimePasswordCommandServiceImpl(UserAccountRepository userAccountRepository,
                                             OneTimePasswordRepository oneTimePasswordRepository,
                                             SecretGenerator secretGenerator,
                                             SecretHasher secretHasher,
                                             EmailNotificationSender emailNotificationSender,
                                             @Value("${iam.otp.digits:6}") int codeDigits,
                                             @Value("${iam.otp.ttl-minutes:10}") long codeLifetimeMinutes) {
        this.userAccountRepository = userAccountRepository;
        this.oneTimePasswordRepository = oneTimePasswordRepository;
        this.secretGenerator = secretGenerator;
        this.secretHasher = secretHasher;
        this.emailNotificationSender = emailNotificationSender;
        this.codeDigits = codeDigits;
        this.codeLifetime = Duration.ofMinutes(codeLifetimeMinutes);
    }

    @Override
    public Result<OneTimePassword, ApplicationError> handle(SendEmailVerificationRequestCommand command) {
        return findAccount(command.userAccountId()).flatMap(account -> {
            if (account.isEmailVerified()) {
                return Result.failure(ApplicationError.businessRuleViolation("email-verification", message(EMAIL_ALREADY_VERIFIED_KEY)));
            }
            return issue(account, OtpPurpose.EMAIL_VERIFICATION);
        });
    }

    @Override
    public Result<OneTimePassword, ApplicationError> handle(TriggerTwoFactorCommand command) {
        return findAccount(command.userAccountId()).flatMap(account -> {
            if (!account.canAuthenticate()) {
                return Result.failure(ApplicationError.businessRuleViolation("two-factor", message(EMAIL_NOT_VERIFIED_KEY)));
            }
            return issue(account, OtpPurpose.LOGIN);
        });
    }

    private Result<OneTimePassword, ApplicationError> issue(UserAccount account, OtpPurpose purpose) {
        var code = secretGenerator.generateNumericCode(codeDigits);
        var expiresAt = Instant.now().plus(codeLifetime);
        var otp = oneTimePasswordRepository.save(
                new OneTimePassword(account.getId(), purpose, secretHasher.hash(code), expiresAt));
        var sent = switch (purpose) {
            case EMAIL_VERIFICATION -> emailNotificationSender.sendEmailVerificationCode(account.getEmail(), code, expiresAt);
            case LOGIN -> emailNotificationSender.sendLoginCode(account.getEmail(), code, expiresAt);
        };
        if (!sent) {
            return Result.failure(ApplicationError.unexpected("email-notification", message(DELIVERY_FAILED_KEY)));
        }
        return Result.success(otp);
    }

    private Result<UserAccount, ApplicationError> findAccount(UUID userAccountId) {
        if (userAccountId == null) {
            return Result.failure(ApplicationError.notFound("UserAccount", "null"));
        }
        return userAccountRepository.findById(new UserAccountId(userAccountId))
                .<Result<UserAccount, ApplicationError>>map(Result::success)
                .orElseGet(() -> Result.failure(ApplicationError.notFound("UserAccount", userAccountId.toString())));
    }

    private static String message(String key) {
        return MessageResolver.resolveOrDefault(key, key);
    }
}
