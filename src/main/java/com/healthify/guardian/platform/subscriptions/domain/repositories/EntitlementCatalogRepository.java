package com.healthify.guardian.platform.subscriptions.domain.repositories;

import com.healthify.guardian.platform.subscriptions.domain.model.entities.Entitlement;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.EntitlementCode;

import java.util.Set;

public interface EntitlementCatalogRepository {

    Set<Entitlement> findAllByCodes(
            Set<EntitlementCode> codes
    );
}