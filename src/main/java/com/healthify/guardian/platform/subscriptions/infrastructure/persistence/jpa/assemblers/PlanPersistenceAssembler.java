package com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.assemblers;

import com.healthify.guardian.platform.subscriptions.domain.model.entities.Plan;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.EntitlementCode;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.Money;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.PlanId;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities.PlanPersistenceEntity;

import java.util.Currency;
import java.util.Set;

public final class PlanPersistenceAssembler {

    private PlanPersistenceAssembler() {
    }

    public static Plan toDomainFromPersistence(
            PlanPersistenceEntity entity,
            Set<EntitlementCode> entitlementCodes
    ) {
        if (entity == null) {
            return null;
        }

        var plan = new Plan();

        plan.setId(
                new PlanId(entity.getId())
        );

        plan.setName(entity.getName());

        plan.setPrice(
                new Money(
                        entity.getPriceAmount(),
                        Currency.getInstance(
                                entity.getPriceCurrency()
                        )
                )
        );

        plan.setBillingCycle(
                entity.getBillingCycle()
        );

        plan.setActive(
                entity.isActive()
        );

        plan.setEntitlementCodes(
                entitlementCodes == null
                        ? Set.of()
                        : Set.copyOf(entitlementCodes)
        );

        return plan;
    }

    public static PlanPersistenceEntity toPersistenceFromDomain(
            Plan plan
    ) {
        if (plan == null) {
            return null;
        }

        return new PlanPersistenceEntity(
                plan.getId().value(),
                plan.getName(),
                plan.getPrice().amount(),
                plan.getPrice().currency().getCurrencyCode(),
                plan.getBillingCycle(),
                plan.isActive()
        );
    }
}