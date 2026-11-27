package com.healthify.guardian.platform.subscriptions.interfaces.rest.transform;

import com.healthify.guardian.platform.subscriptions.domain.model.entities.Plan;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.EntitlementCode;
import com.healthify.guardian.platform.subscriptions.interfaces.rest.resources.PlanResource;

import java.util.LinkedHashSet;
import java.util.stream.Collectors;

public class PlanResourceFromEntityAssembler {

    private PlanResourceFromEntityAssembler() {
    }

    public static PlanResource toResourceFromEntity(
            Plan entity
    ) {

        return new PlanResource(
                entity.getId().value(),
                entity.getName(),
                entity.getPrice().amount(),
                entity.getPrice().currency().getCurrencyCode(),
                entity.getBillingCycle().name(),
                entity.isActive(),
                entity.requiresPayment(),
                entity.getEntitlementCodes()
                        .stream()
                        .map(EntitlementCode::value)
                        .sorted()
                        .collect(
                                Collectors.toCollection(
                                        LinkedHashSet::new
                                )
                        )
        );
    }
}