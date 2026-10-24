package com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.adapters;

import com.healthify.guardian.platform.subscriptions.domain.model.entities.Entitlement;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.EntitlementCode;
import com.healthify.guardian.platform.subscriptions.domain.repositories.EntitlementCatalogRepository;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.assemblers.EntitlementPersistenceAssembler;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.repositories.EntitlementPersistenceRepository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class EntitlementCatalogRepositoryImpl
        implements EntitlementCatalogRepository {

    private final EntitlementPersistenceRepository
            entitlementPersistenceRepository;

    public EntitlementCatalogRepositoryImpl(
            EntitlementPersistenceRepository entitlementPersistenceRepository
    ) {
        this.entitlementPersistenceRepository =
                entitlementPersistenceRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Entitlement> findAllByCodes(
            Set<EntitlementCode> codes
    ) {

        if (codes == null || codes.isEmpty()) {
            return Set.of();
        }

        List<String> rawCodes =
                codes.stream()
                        .map(EntitlementCode::value)
                        .toList();

        return entitlementPersistenceRepository
                .findAllById(rawCodes)
                .stream()
                .map(
                        EntitlementPersistenceAssembler
                                ::toDomainFromPersistence
                )
                .collect(
                        Collectors.toSet()
                );
    }
}