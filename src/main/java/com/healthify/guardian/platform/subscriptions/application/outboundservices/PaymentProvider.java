package com.healthify.guardian.platform.subscriptions.application.outboundservices;

public interface PaymentProvider {

    PaymentInitiationResult initiatePayment(PaymentRequest request);
}