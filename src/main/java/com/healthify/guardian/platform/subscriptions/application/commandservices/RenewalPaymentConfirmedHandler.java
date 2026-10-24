package com.healthify.guardian.platform.subscriptions.application.commandservices;

import com.healthify.guardian.platform.subscriptions.application.commands.ConfirmRenewalPaymentCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.Subscription;

public interface RenewalPaymentConfirmedHandler {

    Subscription handle(
            ConfirmRenewalPaymentCommand command
    );
}