package com.healthify.guardian.platform.iam.application.internal.eventhandlers;

import com.healthify.guardian.platform.iam.domain.model.events.EmailVerifiedEvent;
import com.healthify.guardian.platform.iam.interfaces.events.UserAccountVerifiedIntegrationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Announces to other contexts that an account verified its e-mail and is now active.
 */
@Service
public class EmailVerifiedEventHandler {

    private final ApplicationEventPublisher eventPublisher;

    public EmailVerifiedEventHandler(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    public void on(EmailVerifiedEvent event) {
        eventPublisher.publishEvent(new UserAccountVerifiedIntegrationEvent(
                event.userAccountId().value(), event.email().value(), event.verifiedAt()));
    }
}
