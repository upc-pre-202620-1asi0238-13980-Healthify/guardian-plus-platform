package com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities;

import com.healthify.guardian.platform.subscriptions.application.outboundservices.PaymentPurpose;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "subscription_payment_operations",
        indexes = {
                @Index(
                        name = "ix_subscription_payment_operations_subscription_id",
                        columnList = "subscription_id"
                )
        }
)
public class PaymentOperationPersistenceEntity {

    @Id
    @Column(
            name = "idempotency_key",
            nullable = false,
            updatable = false,
            length = 255
    )
    private String idempotencyKey;

    @Column(
            name = "subscription_id",
            nullable = false,
            updatable = false
    )
    private UUID subscriptionId;

    @Column(
            name = "subscriber_id",
            nullable = false,
            updatable = false
    )
    private UUID subscriberId;

    @Column(
            name = "plan_id",
            nullable = false,
            updatable = false
    )
    private UUID planId;

    @Column(
            name = "provider",
            nullable = false,
            updatable = false,
            length = 50
    )
    private String provider;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "purpose",
            nullable = false,
            updatable = false,
            length = 50
    )
    private PaymentPurpose purpose;

    @Column(
            name = "amount",
            nullable = false,
            updatable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal amount;

    @Column(
            name = "currency",
            nullable = false,
            updatable = false,
            length = 3
    )
    private String currency;

    @Column(
            name = "current_period_end",
            updatable = false
    )
    private Instant currentPeriodEnd;

    @Column(
            name = "external_reference",
            unique = true,
            length = 255
    )
    private String externalReference;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;

    protected PaymentOperationPersistenceEntity() {
    }

    public PaymentOperationPersistenceEntity(
            String idempotencyKey,
            UUID subscriptionId,
            UUID subscriberId,
            UUID planId,
            String provider,
            PaymentPurpose purpose,
            BigDecimal amount,
            String currency,
            Instant currentPeriodEnd,
            Instant createdAt
    ) {
        this.idempotencyKey =
                Objects.requireNonNull(idempotencyKey);

        this.subscriptionId =
                Objects.requireNonNull(subscriptionId);

        this.subscriberId =
                Objects.requireNonNull(subscriberId);

        this.planId =
                Objects.requireNonNull(planId);

        this.provider =
                Objects.requireNonNull(provider);

        this.purpose =
                Objects.requireNonNull(purpose);

        this.amount =
                Objects.requireNonNull(amount);

        this.currency =
                Objects.requireNonNull(currency);

        this.currentPeriodEnd =
                currentPeriodEnd;

        this.externalReference =
                null;

        this.createdAt =
                Objects.requireNonNull(createdAt);

        this.updatedAt =
                createdAt;
    }

    public void registerExternalReference(
            String externalReference,
            Instant updatedAt
    ) {

        Objects.requireNonNull(
                externalReference,
                "externalReference must not be null"
        );

        Objects.requireNonNull(
                updatedAt,
                "updatedAt must not be null"
        );

        if (externalReference.isBlank()) {

            throw new IllegalArgumentException(
                    "externalReference must not be blank"
            );
        }

        if (this.externalReference != null) {

            if (!this.externalReference.equals(externalReference)) {

                throw new IllegalStateException(
                        "Payment operation already has a different external reference"
                );
            }

            return;
        }

        this.externalReference =
                externalReference;

        this.updatedAt =
                updatedAt;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public UUID getSubscriberId() {
        return subscriberId;
    }

    public UUID getPlanId() {
        return planId;
    }

    public String getProvider() {
        return provider;
    }

    public PaymentPurpose getPurpose() {
        return purpose;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public Instant getCurrentPeriodEnd() {
        return currentPeriodEnd;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}