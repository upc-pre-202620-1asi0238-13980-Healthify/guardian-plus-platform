package com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.adapters;

import com.healthify.guardian.platform.subscriptions.domain.model.entities.Plan;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.EntitlementCode;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.PlanId;
import com.healthify.guardian.platform.subscriptions.domain.repositories.PlanRepository;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.assemblers.PlanPersistenceAssembler;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.repositories.PlanEntitlementPersistenceRepository;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.repositories.PlanPersistenceRepository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class PlanRepositoryImpl implements PlanRepository {

    private final PlanPersistenceRepository planPersistenceRepository;
    private final PlanEntitlementPersistenceRepository planEntitlementPersistenceRepository;

    public PlanRepositoryImpl(
            PlanPersistenceRepository planPersistenceRepository,
            PlanEntitlementPersistenceRepository planEntitlementPersistenceRepository
    ) {
        this.planPersistenceRepository = planPersistenceRepository;
        this.planEntitlementPersistenceRepository =
                planEntitlementPersistenceRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Plan> findById(PlanId id) {

        return planPersistenceRepository
                .findById(id.value())
                .map(entity -> {

                    Set<EntitlementCode> entitlementCodes =
                            planEntitlementPersistenceRepository
                                    .findAllByPlan_Id(entity.getId())
                                    .stream()
                                    .map(relation ->
                                            new EntitlementCode(
                                                    relation
                                                            .getEntitlement()
                                                            .getCode()
                                            )
                                    )
                                    .collect(Collectors.toSet());

                    return PlanPersistenceAssembler
                            .toDomainFromPersistence(
                                    entity,
                                    entitlementCodes
                            );
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<Plan> findActivePlans() {

        return planPersistenceRepository
                .findAllByActiveTrue()
                .stream()
                .map(entity -> {

                    Set<EntitlementCode> entitlementCodes =
                            planEntitlementPersistenceRepository
                                    .findAllByPlan_Id(entity.getId())
                                    .stream()
                                    .map(relation ->
                                            new EntitlementCode(
                                                    relation
                                                            .getEntitlement()
                                                            .getCode()
                                            )
                                    )
                                    .collect(Collectors.toSet());

                    return PlanPersistenceAssembler
                            .toDomainFromPersistence(
                                    entity,
                                    entitlementCodes
                            );
                })
                .toList();
    }
}