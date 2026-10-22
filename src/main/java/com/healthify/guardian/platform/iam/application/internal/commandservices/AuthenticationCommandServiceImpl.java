package com.healthify.guardian.platform.iam.application.internal.commandservices;

import com.healthify.guardian.platform.iam.application.commandservices.AuthenticationCommandService;
import com.healthify.guardian.platform.iam.application.outboundservices.SessionToken;
import com.healthify.guardian.platform.iam.application.outboundservices.SessionTokenService;
import com.healthify.guardian.platform.iam.domain.model.aggregates.UserAccount;
import com.healthify.guardian.platform.iam.domain.model.commands.AuthenticateUserCommand;
import com.healthify.guardian.platform.iam.domain.model.commands.LoginCommand;
import com.healthify.guardian.platform.iam.domain.model.commands.VerifyOtpCommand;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.Email;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.OtpPurpose;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.UserAccountId;
import com.healthify.guardian.platform.iam.domain.repositories.OneTimePasswordRepository;
import com.healthify.guardian.platform.iam.domain.repositories.UserAccountRepository;
import com.healthify.guardian.platform.iam.domain.services.PasswordHasher;
import com.healthify.guardian.platform.iam.domain.services.SecretHasher;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;
import com.healthify.guardian.platform.shared.infrastructure.i18n.MessageResolver;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

/**
 * Application service for the two-step login of the IAM EventStorming:
 * {@code Login → User Logged in → (Credentials correct?) → Trigger 2FA → OTP Sent to Email → (OTP correct?) →
 * Authenticate User → User Authenticated}.
 *
 * <p>The step from "User Logged in" to "Trigger 2FA" runs as a policy ({@code UserLoggedInEventHandler}). The step
 * from "OTP correct?" to "Authenticate User" runs here, because the session token it produces is the response of
 * the OTP request.</p>
 */
@Service
public class AuthenticationCommandServiceImpl implements AuthenticationCommandService {

    private static final String CREDENTIALS_INVALID_KEY = "user-account.credentials.invalid";
    private static final String OTP_INVALID_KEY = "otp.code.invalid";

    private final UserAccountRepository userAccountRepository;
    private final OneTimePasswordRepository oneTimePasswordRepository;
    private final PasswordHasher passwordHasher;
    private final SecretHasher secretHasher;
    private final SessionTokenService sessionTokenService;

    public AuthenticationCommandServiceImpl(UserAccountRepository userAccountRepository,
                                            OneTimePasswordRepository oneTimePasswordRepository,
                                            PasswordHasher passwordHasher,
                                            SecretHasher secretHasher,
                                            SessionTokenService sessionTokenService) {
        this.userAccountRepository = userAccountRepository;
        this.oneTimePasswordRepository = oneTimePasswordRepository;
        this.passwordHasher = passwordHasher;
        this.secretHasher = secretHasher;
        this.sessionTokenService = sessionTokenService;
    }

    @Override
    public Result<UserAccount, ApplicationError> handle(LoginCommand command) {
        // Same answer for an unknown e-mail and a wrong password, so a login does not reveal which e-mails exist
        var account = parseEmail(command.email()).flatMap(userAccountRepository::findByEmail);
        if (account.isEmpty()) {
            return authenticationFailed(CREDENTIALS_INVALID_KEY);
        }
        try {
            if (!account.get().logIn(command.rawPassword(), passwordHasher)) {
                return authenticationFailed(CREDENTIALS_INVALID_KEY);
            }
        } catch (IllegalStateException e) {
            // Only reached with the right password, so revealing the account id is safe
            return Result.failure(new ApplicationError(EMAIL_NOT_VERIFIED, message(e.getMessage()),
                    account.get().getId().value().toString()));
        }
        // Saving publishes UserLoggedInEvent, whose policy sends the OTP
        return Result.success(userAccountRepository.save(account.get()));
    }

    @Override
    public Result<SessionToken, ApplicationError> handle(VerifyOtpCommand command) {
        if (command.userAccountId() == null || command.code() == null) {
            return authenticationFailed(OTP_INVALID_KEY);
        }
        var userAccountId = new UserAccountId(command.userAccountId());
        var now = Instant.now();
        var otp = oneTimePasswordRepository.findLatestUsableByUserAccountIdAndPurpose(userAccountId, OtpPurpose.LOGIN, now);
        if (otp.isEmpty()) {
            return authenticationFailed(OTP_INVALID_KEY);
        }
        var correct = otp.get().verify(command.code(), secretHasher, now);
        // Saved either way: a match consumes the code and a mismatch counts as a failed attempt
        oneTimePasswordRepository.save(otp.get());
        if (!correct) {
            return authenticationFailed(OTP_INVALID_KEY);
        }
        return handle(new AuthenticateUserCommand(command.userAccountId()));
    }

    @Override
    public Result<SessionToken, ApplicationError> handle(AuthenticateUserCommand command) {
        if (command.userAccountId() == null) {
            return Result.failure(ApplicationError.notFound("UserAccount", "null"));
        }
        var account = userAccountRepository.findById(new UserAccountId(command.userAccountId()));
        if (account.isEmpty()) {
            return Result.failure(ApplicationError.notFound("UserAccount", command.userAccountId().toString()));
        }
        try {
            account.get().authenticate();
        } catch (IllegalStateException e) {
            return Result.failure(ApplicationError.businessRuleViolation("authentication", message(e.getMessage())));
        }
        var authenticated = userAccountRepository.save(account.get());
        return Result.success(sessionTokenService.issue(authenticated));
    }

    private static Optional<Email> parseEmail(String email) {
        try {
            return Optional.of(new Email(email));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private static <T> Result<T, ApplicationError> authenticationFailed(String key) {
        return Result.failure(new ApplicationError(AUTHENTICATION_FAILED, message(key)));
    }

    private static String message(String key) {
        return MessageResolver.resolveOrDefault(key, key);
    }
}
