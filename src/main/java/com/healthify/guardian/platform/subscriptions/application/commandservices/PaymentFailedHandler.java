package com.healthify.guardian.platform.subscriptions.application.commandservices;

import com.healthify.guardian.platform.subscriptions.application.commands.FailSubscriptionPaymentCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.Subscription;

public interface PaymentFailedHandler {

    Subscription handle(
            FailSubscriptionPaymentCommand command
    );
}