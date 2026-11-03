package com.healthify.guardian.platform.iam.application.internal.eventhandlers;

import com.healthify.guardian.platform.iam.application.commandservices.OneTimePasswordCommandService;
import com.healthify.guardian.platform.iam.domain.model.commands.TriggerTwoFactorCommand;
import com.healthify.guardian.platform.iam.domain.model.events.UserLoggedInEvent;
import com.healthify.guardian.platform.shared.application.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Implements the <b>Credentials correct?</b> policy: once a user logged in with the right e-mail and password, it
 * triggers the second factor, which e-mails the OTP. The user is not authenticated until that OTP is verified.
 */
@Slf4j
@Service
public class UserLoggedInEventHandler {

    private final OneTimePasswordCommandService oneTimePasswordCommandService;

    public UserLoggedInEventHandler(OneTimePasswordCommandService oneTimePasswordCommandService) {
        this.oneTimePasswordCommandService = oneTimePasswordCommandService;
    }

    @EventListener
    public void on(UserLoggedInEvent event) {
        try {
            var result = oneTimePasswordCommandService.handle(new TriggerTwoFactorCommand(event.userAccountId().value()));
            if (result instanceof Result.Failure<?, ?> failure) {
                log.warn("Login OTP of account {} not sent: {}", event.userAccountId().value(), failure.error());
            }
        } catch (RuntimeException e) {
            log.error("Login OTP of account {} failed", event.userAccountId().value(), e);
        }
    }
}
