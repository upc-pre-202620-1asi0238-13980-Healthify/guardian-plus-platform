package com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities;

import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.BillingCycle;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "subscription_plans")
public class PlanPersistenceEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(
            name = "price_amount",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal priceAmount;

    @Column(
            name = "price_currency",
            nullable = false,
            length = 3
    )
    private String priceCurrency;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "billing_cycle",
            nullable = false,
            length = 20
    )
    private BillingCycle billingCycle;

    @Column(name = "active", nullable = false)
    private boolean active;

    protected PlanPersistenceEntity() {
    }

    public PlanPersistenceEntity(
            UUID id,
            String name,
            BigDecimal priceAmount,
            String priceCurrency,
            BillingCycle billingCycle,
            boolean active
    ) {
        this.id = id;
        this.name = name;
        this.priceAmount = priceAmount;
        this.priceCurrency = priceCurrency;
        this.billingCycle = billingCycle;
        this.active = active;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPriceAmount() {
        return priceAmount;
    }

    public String getPriceCurrency() {
        return priceCurrency;
    }

    public BillingCycle getBillingCycle() {
        return billingCycle;
    }

    public boolean isActive() {
        return active;
    }
}