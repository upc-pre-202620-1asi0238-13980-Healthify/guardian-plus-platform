package com.healthify.guardian.platform.iam.application.commandservices;

import com.healthify.guardian.platform.iam.domain.model.aggregates.OneTimePassword;
import com.healthify.guardian.platform.iam.domain.model.commands.SendEmailVerificationRequestCommand;
import com.healthify.guardian.platform.iam.domain.model.commands.TriggerTwoFactorCommand;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;

/**
 * Application service contract that issues one-time codes and e-mails them.
 */
public interface OneTimePasswordCommandService {

    /**
     * Sends the code that verifies the e-mail of an account still pending verification.
     */
    Result<OneTimePassword, ApplicationError> handle(SendEmailVerificationRequestCommand command);

    /**
     * Sends the second-factor code (OTP) of a login.
     */
    Result<OneTimePassword, ApplicationError> handle(TriggerTwoFactorCommand command);
}
