package com.healthify.guardian.platform.iam.application.internal.eventhandlers;

import com.healthify.guardian.platform.iam.application.commandservices.OneTimePasswordCommandService;
import com.healthify.guardian.platform.iam.domain.model.commands.SendEmailVerificationRequestCommand;
import com.healthify.guardian.platform.iam.domain.model.events.UserCredentialsRegisteredEvent;
import com.healthify.guardian.platform.iam.interfaces.events.UserAccountRegisteredIntegrationEvent;
import com.healthify.guardian.platform.shared.application.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Implements the policy after <b>Credentials Registered</b>: sends the e-mail verification request and announces the
 * new identity to other contexts. A failed e-mail does not undo the registration; the user can ask for a new code.
 */
@Slf4j
@Service
public class UserCredentialsRegisteredEventHandler {

    private final OneTimePasswordCommandService oneTimePasswordCommandService;
    private final ApplicationEventPublisher eventPublisher;

    public UserCredentialsRegisteredEventHandler(OneTimePasswordCommandService oneTimePasswordCommandService,
                                                 ApplicationEventPublisher eventPublisher) {
        this.oneTimePasswordCommandService = oneTimePasswordCommandService;
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    public void on(UserCredentialsRegisteredEvent event) {
        try {
            var result = oneTimePasswordCommandService.handle(
                    new SendEmailVerificationRequestCommand(event.userAccountId().value()));
            if (result instanceof Result.Failure<?, ?> failure) {
                log.warn("E-mail verification request of account {} not sent: {}", event.userAccountId().value(), failure.error());
            }
        } catch (RuntimeException e) {
            log.error("E-mail verification request of account {} failed", event.userAccountId().value(), e);
        }
        eventPublisher.publishEvent(new UserAccountRegisteredIntegrationEvent(
                event.userAccountId().value(), event.email().value(), event.registeredAt()));
    }
}
