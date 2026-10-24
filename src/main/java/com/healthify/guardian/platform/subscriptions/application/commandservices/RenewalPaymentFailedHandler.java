package com.healthify.guardian.platform.subscriptions.application.commandservices;

import com.healthify.guardian.platform.subscriptions.application.commands.FailRenewalPaymentCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.Subscription;

public interface RenewalPaymentFailedHandler {

    Subscription handle(
            FailRenewalPaymentCommand command
    );
}