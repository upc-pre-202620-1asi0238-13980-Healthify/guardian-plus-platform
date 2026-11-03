package com.healthify.guardian.platform.iam.application.commandservices;

import com.healthify.guardian.platform.iam.domain.model.aggregates.UserAccount;
import com.healthify.guardian.platform.iam.domain.model.commands.RegisterUserCredentialsCommand;
import com.healthify.guardian.platform.iam.domain.model.commands.RequestPasswordResetCommand;
import com.healthify.guardian.platform.iam.domain.model.commands.ResetPasswordCommand;
import com.healthify.guardian.platform.iam.domain.model.commands.VerifyEmailCommand;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;

/**
 * Application service contract for commands over the {@code UserAccount} aggregate.
 */
public interface UserAccountCommandService {

    /**
     * Registers new credentials pending e-mail verification. An e-mail can only be registered once.
     */
    Result<UserAccount, ApplicationError> handle(RegisterUserCredentialsCommand command);

    /**
     * Confirms the e-mail with its verification code and activates the account.
     */
    Result<UserAccount, ApplicationError> handle(VerifyEmailCommand command);

    /**
     * Generates a password reset token and e-mails it. Fails if no account uses the e-mail.
     */
    Result<UserAccount, ApplicationError> handle(RequestPasswordResetCommand command);

    /**
     * Sets a new password with a valid reset token.
     */
    Result<UserAccount, ApplicationError> handle(ResetPasswordCommand command);
}
