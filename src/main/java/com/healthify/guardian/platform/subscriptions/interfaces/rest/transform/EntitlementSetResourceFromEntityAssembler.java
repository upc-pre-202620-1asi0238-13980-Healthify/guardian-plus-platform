package com.healthify.guardian.platform.subscriptions.interfaces.rest.transform;

import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.EntitlementSet;
import com.healthify.guardian.platform.subscriptions.interfaces.rest.resources.EntitlementResource;
import com.healthify.guardian.platform.subscriptions.interfaces.rest.resources.EntitlementSetResource;

import java.util.Set;
import java.util.stream.Collectors;

public final class EntitlementSetResourceFromEntityAssembler {

    private EntitlementSetResourceFromEntityAssembler() {
    }

    public static EntitlementSetResource toResourceFromEntity(
            EntitlementSet entitlementSet
    ) {

        Set<EntitlementResource> entitlements =
                entitlementSet.getEntitlements() == null
                        ? Set.of()
                        : entitlementSet
                        .getEntitlements()
                        .stream()
                        .map(entitlement ->
                                new EntitlementResource(
                                        entitlement
                                                .getCode()
                                                .value(),
                                        entitlement.getName(),
                                        entitlement.getDescription()
                                )
                        )
                        .collect(
                                Collectors.toSet()
                        );

        return new EntitlementSetResource(
                entitlementSet
                        .getSubscriptionId()
                        .value(),
                entitlements,
                entitlementSet.getUpdatedAt()
        );
    }
}