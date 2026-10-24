package com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.assemblers;

import com.healthify.guardian.platform.subscriptions.domain.model.entities.Entitlement;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.EntitlementCode;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities.EntitlementPersistenceEntity;

public final class EntitlementPersistenceAssembler {

    private EntitlementPersistenceAssembler() {
    }

    public static Entitlement toDomainFromPersistence(
            EntitlementPersistenceEntity entity
    ) {
        if (entity == null) {
            return null;
        }

        var entitlement = new Entitlement();

        entitlement.setCode(
                new EntitlementCode(entity.getCode())
        );

        entitlement.setName(entity.getName());
        entitlement.setDescription(entity.getDescription());

        return entitlement;
    }

    public static EntitlementPersistenceEntity toPersistenceFromDomain(
            Entitlement entitlement
    ) {
        if (entitlement == null) {
            return null;
        }

        return new EntitlementPersistenceEntity(
                entitlement.getCode().value(),
                entitlement.getName(),
                entitlement.getDescription()
        );
    }
}