package com.healthify.guardian.platform.subscriptions.application.commandservices;

import com.healthify.guardian.platform.subscriptions.application.commands.InitiateSubscriptionPaymentCommand;
import com.healthify.guardian.platform.subscriptions.application.outboundservices.PaymentInitiationResult;

public interface InitiateSubscriptionPaymentHandler {

    PaymentInitiationResult handle(
            InitiateSubscriptionPaymentCommand command
    );
}