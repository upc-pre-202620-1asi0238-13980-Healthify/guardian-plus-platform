package com.healthify.guardian.platform.iam.application.commandservices;

import com.healthify.guardian.platform.iam.application.outboundservices.SessionToken;
import com.healthify.guardian.platform.iam.domain.model.aggregates.UserAccount;
import com.healthify.guardian.platform.iam.domain.model.commands.AuthenticateUserCommand;
import com.healthify.guardian.platform.iam.domain.model.commands.LoginCommand;
import com.healthify.guardian.platform.iam.domain.model.commands.VerifyOtpCommand;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;

/**
 * Application service contract for the two-step login: password first, then the OTP sent by e-mail.
 */
public interface AuthenticationCommandService {

    /** Error code of a failed login or OTP check; the interface layer maps it to {@code 401 Unauthorized}. */
    String AUTHENTICATION_FAILED = "AUTHENTICATION_FAILED";

    /**
     * Checks the e-mail and password. On success the account is logged in and the OTP is sent to its e-mail.
     */
    Result<UserAccount, ApplicationError> handle(LoginCommand command);

    /**
     * Checks the OTP of a login and, if it is correct, authenticates the user.
     */
    Result<SessionToken, ApplicationError> handle(VerifyOtpCommand command);

    /**
     * Authenticates the user and issues the signed session token.
     */
    Result<SessionToken, ApplicationError> handle(AuthenticateUserCommand command);
}
