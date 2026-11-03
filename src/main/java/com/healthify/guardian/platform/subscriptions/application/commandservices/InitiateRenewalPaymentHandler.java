package com.healthify.guardian.platform.subscriptions.application.commandservices;

import com.healthify.guardian.platform.subscriptions.application.commands.InitiateRenewalPaymentCommand;
import com.healthify.guardian.platform.subscriptions.application.outboundservices.PaymentInitiationResult;

public interface InitiateRenewalPaymentHandler {

    PaymentInitiationResult handle(
            InitiateRenewalPaymentCommand command
    );
}