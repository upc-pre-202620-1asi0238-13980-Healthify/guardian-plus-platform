package com.healthify.guardian.platform.iam.application.internal.eventhandlers;

import com.healthify.guardian.platform.iam.domain.model.events.UserSignedUpWithGoogleEvent;
import com.healthify.guardian.platform.iam.interfaces.events.UserAccountRegisteredIntegrationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Announces to other contexts the identity created by a Google sign-in, like a registration with a password.
 * No verification code is sent: Google already verified the e-mail.
 */
@Service
public class UserSignedUpWithGoogleEventHandler {

    private final ApplicationEventPublisher eventPublisher;

    public UserSignedUpWithGoogleEventHandler(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    public void on(UserSignedUpWithGoogleEvent event) {
        eventPublisher.publishEvent(new UserAccountRegisteredIntegrationEvent(
                event.userAccountId().value(), event.email().value(), event.signedUpAt()));
    }
}
