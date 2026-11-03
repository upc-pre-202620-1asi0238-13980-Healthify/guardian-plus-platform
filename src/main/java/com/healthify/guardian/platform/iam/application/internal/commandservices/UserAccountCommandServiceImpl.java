package com.healthify.guardian.platform.iam.application.internal.commandservices;

import com.healthify.guardian.platform.iam.application.commandservices.UserAccountCommandService;
import com.healthify.guardian.platform.iam.application.outboundservices.EmailNotificationSender;
import com.healthify.guardian.platform.iam.domain.model.aggregates.UserAccount;
import com.healthify.guardian.platform.iam.domain.model.commands.RegisterUserCredentialsCommand;
import com.healthify.guardian.platform.iam.domain.model.commands.RequestPasswordResetCommand;
import com.healthify.guardian.platform.iam.domain.model.commands.ResetPasswordCommand;
import com.healthify.guardian.platform.iam.domain.model.commands.VerifyEmailCommand;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.Email;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.OtpPurpose;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.UserAccountId;
import com.healthify.guardian.platform.iam.domain.repositories.OneTimePasswordRepository;
import com.healthify.guardian.platform.iam.domain.repositories.UserAccountRepository;
import com.healthify.guardian.platform.iam.domain.services.PasswordHasher;
import com.healthify.guardian.platform.iam.domain.services.SecretGenerator;
import com.healthify.guardian.platform.iam.domain.services.SecretHasher;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;
import com.healthify.guardian.platform.shared.infrastructure.i18n.MessageResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/**
 * Application service that executes user account commands: registration, e-mail verification and password reset.
 */
@Service
public class UserAccountCommandServiceImpl implements UserAccountCommandService {

    private static final String EMAIL_TAKEN_KEY = "user-account.email.taken";
    private static final String CODE_INVALID_KEY = "otp.code.invalid";
    private static final String RESET_TOKEN_INVALID_KEY = "password-reset-token.invalid";
    private static final String DELIVERY_FAILED_KEY = "iam.email.delivery-failed";

    private final UserAccountRepository userAccountRepository;
    private final OneTimePasswordRepository oneTimePasswordRepository;
    private final PasswordHasher passwordHasher;
    private final SecretHasher secretHasher;
    private final SecretGenerator secretGenerator;
    private final EmailNotificationSender emailNotificationSender;
    private final Duration resetTokenLifetime;

    public UserAccountCommandServiceImpl(UserAccountRepository userAccountRepository,
                                         OneTimePasswordRepository oneTimePasswordRepository,
                                         PasswordHasher passwordHasher,
                                         SecretHasher secretHasher,
                                         SecretGenerator secretGenerator,
                                         EmailNotificationSender emailNotificationSender,
                                         @Value("${iam.password-reset.ttl-minutes:30}") long resetTokenLifetimeMinutes) {
        this.userAccountRepository = userAccountRepository;
        this.oneTimePasswordRepository = oneTimePasswordRepository;
        this.passwordHasher = passwordHasher;
        this.secretHasher = secretHasher;
        this.secretGenerator = secretGenerator;
        this.emailNotificationSender = emailNotificationSender;
        this.resetTokenLifetime = Duration.ofMinutes(resetTokenLifetimeMinutes);
    }

    @Override
    public Result<UserAccount, ApplicationError> handle(RegisterUserCredentialsCommand command) {
        try {
            // Email Available? policy, checked before hashing so a taken e-mail costs no BCrypt round
            if (userAccountRepository.existsByEmail(new Email(command.email()))) {
                return Result.failure(ApplicationError.conflict("UserAccount", message(EMAIL_TAKEN_KEY)));
            }
            var account = new UserAccount(command, passwordHasher.hash(command.rawPassword()));
            // Saving publishes UserCredentialsRegisteredEvent, whose policy sends the verification code
            return Result.success(userAccountRepository.save(account));
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("register-user-credentials", message(e.getMessage())));
        }
    }

    @Override
    public Result<UserAccount, ApplicationError> handle(VerifyEmailCommand command) {
        if (command.userAccountId() == null) {
            return Result.failure(ApplicationError.notFound("UserAccount", "null"));
        }
        var userAccountId = new UserAccountId(command.userAccountId());
        var account = userAccountRepository.findById(userAccountId);
        if (account.isEmpty()) {
            return Result.failure(ApplicationError.notFound("UserAccount", command.userAccountId().toString()));
        }
        if (account.get().isEmailVerified()) {
            return Result.failure(ApplicationError.businessRuleViolation("verify-email",
                    message("user-account.email.already-verified")));
        }
        var now = Instant.now();
        var otp = oneTimePasswordRepository.findLatestUsableByUserAccountIdAndPurpose(
                userAccountId, OtpPurpose.EMAIL_VERIFICATION, now);
        if (otp.isEmpty()) {
            return Result.failure(ApplicationError.validationError("code", message(CODE_INVALID_KEY)));
        }
        var correct = otp.get().verify(command.code(), secretHasher, now);
        // Saved either way: a match consumes the code and a mismatch counts as a failed attempt
        oneTimePasswordRepository.save(otp.get());
        if (!correct) {
            return Result.failure(ApplicationError.validationError("code", message(CODE_INVALID_KEY)));
        }
        account.get().confirmEmailVerification();
        return Result.success(userAccountRepository.save(account.get()));
    }

    @Override
    public Result<UserAccount, ApplicationError> handle(RequestPasswordResetCommand command) {
        Email email;
        try {
            email = new Email(command.email());
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("email", message(e.getMessage())));
        }
        // Per the EventStorming, an unknown e-mail is reported back as an error
        var account = userAccountRepository.findByEmail(email);
        if (account.isEmpty()) {
            return Result.failure(ApplicationError.notFound("UserAccount", email.value()));
        }
        var token = secretGenerator.generateOpaqueToken();
        var expiresAt = Instant.now().plus(resetTokenLifetime);
        account.get().generatePasswordResetToken(secretHasher.hash(token), expiresAt);
        var saved = userAccountRepository.save(account.get());
        if (!emailNotificationSender.sendPasswordResetToken(saved.getEmail(), token, expiresAt)) {
            return Result.failure(ApplicationError.unexpected("email-notification", message(DELIVERY_FAILED_KEY)));
        }
        return Result.success(saved);
    }

    @Override
    public Result<UserAccount, ApplicationError> handle(ResetPasswordCommand command) {
        if (command.token() == null || command.token().isBlank()) {
            return Result.failure(ApplicationError.validationError("token", message(RESET_TOKEN_INVALID_KEY)));
        }
        var account = userAccountRepository.findByPasswordResetTokenHash(secretHasher.hash(command.token()));
        if (account.isEmpty()) {
            return Result.failure(ApplicationError.validationError("token", message(RESET_TOKEN_INVALID_KEY)));
        }
        try {
            account.get().resetPassword(passwordHasher.hash(command.newRawPassword()), Instant.now());
        } catch (IllegalStateException | IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("token", message(e.getMessage())));
        }
        return Result.success(userAccountRepository.save(account.get()));
    }

    private static String message(String key) {
        return MessageResolver.resolveOrDefault(key, key);
    }
}
