package com.healthify.guardian.platform.subscriptions.domain.model.valueobjects;

import java.math.BigDecimal;
import java.util.Currency;

public record Money(
        BigDecimal amount,
        Currency currency) {

    public Money {
        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "subscription.money.amount.invalid");
        }

        if (currency == null) {
            throw new IllegalArgumentException(
                    "subscription.money.currency.invalid");
        }
    }

    public boolean isZero() {
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }
}