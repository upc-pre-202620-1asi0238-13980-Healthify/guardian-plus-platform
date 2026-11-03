package com.healthify.guardian.platform.subscriptions.application.exceptions;

public class SubscriptionPaymentException extends RuntimeException {

    public SubscriptionPaymentException(String message) {
        super(message);
    }
}