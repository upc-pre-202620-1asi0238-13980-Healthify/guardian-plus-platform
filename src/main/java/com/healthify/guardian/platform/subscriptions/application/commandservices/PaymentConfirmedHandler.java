package com.healthify.guardian.platform.subscriptions.application.commandservices;

import com.healthify.guardian.platform.subscriptions.application.commands.ConfirmSubscriptionPaymentCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.Subscription;

public interface PaymentConfirmedHandler {

    Subscription handle(
            ConfirmSubscriptionPaymentCommand command
    );
}