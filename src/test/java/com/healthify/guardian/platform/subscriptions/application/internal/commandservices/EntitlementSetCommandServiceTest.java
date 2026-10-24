package com.healthify.guardian.platform.subscriptions.application.internal.commandservices;

import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.EntitlementSet;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.EntitlementDefinition;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.RevokeAllEntitlementsCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.UpdateEntitlementsCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.entities.Entitlement;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.EntitlementCode;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriptionId;
import com.healthify.guardian.platform.subscriptions.domain.repositories.EntitlementSetRepository;
import com.healthify.guardian.platform.subscriptions.domain.repositories.PlanRepository;
import com.healthify.guardian.platform.subscriptions.domain.repositories.EntitlementCatalogRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EntitlementSetCommandServiceTest {

    @Mock
    private EntitlementSetRepository entitlementSetRepository;

    private EntitlementSetCommandServiceImpl entitlementSetCommandService;

    @Mock
    private PlanRepository planRepository;

    @Mock
    private EntitlementCatalogRepository entitlementCatalogRepository;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        entitlementSetCommandService =
                new EntitlementSetCommandServiceImpl(
                        entitlementSetRepository,
                        planRepository,
                        entitlementCatalogRepository
                );

        when(entitlementSetRepository.save(
                any(EntitlementSet.class)
        )).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );
    }

    @Test
    void shouldUpdateEffectiveEntitlements() {

        UUID subscriptionUuid =
                UUID.randomUUID();

        EntitlementSet existing =
                createEmptyEntitlementSet(
                        subscriptionUuid
                );

        when(entitlementSetRepository.findBySubscriptionId(
                new SubscriptionId(subscriptionUuid)
        )).thenReturn(
                Optional.of(existing)
        );

        Set<EntitlementDefinition> definitions =
                Set.of(
                        new EntitlementDefinition(
                                "REAL_TIME_LOCATION",
                                "Real-time location",
                                "Allows real-time location monitoring"
                        ),
                        new EntitlementDefinition(
                                "HEALTH_MONITORING",
                                "Health monitoring",
                                "Allows health monitoring capabilities"
                        )
                );

        var result =
                entitlementSetCommandService.handle(
                        new UpdateEntitlementsCommand(
                                subscriptionUuid,
                                definitions
                        )
                );

        assertTrue(
                result.isSuccess()
        );

        EntitlementSet updated =
                result.toOptional()
                        .orElseThrow();

        assertEquals(
                new SubscriptionId(subscriptionUuid),
                updated.getSubscriptionId()
        );

        assertEquals(
                2,
                updated.getEntitlements().size()
        );

        assertTrue(
                updated.contains(
                        new EntitlementCode(
                                "REAL_TIME_LOCATION"
                        )
                )
        );

        assertTrue(
                updated.contains(
                        new EntitlementCode(
                                "HEALTH_MONITORING"
                        )
                )
        );

        assertNotNull(
                updated.getUpdatedAt()
        );

        verify(entitlementSetRepository)
                .save(existing);
    }

    @Test
    void shouldReplacePreviousEntitlements() {

        UUID subscriptionUuid =
                UUID.randomUUID();

        EntitlementSet existing =
                createEmptyEntitlementSet(
                        subscriptionUuid
                );

        existing.replaceWith(
                Set.of(
                        createEntitlement(
                                "OLD_FEATURE",
                                "Old feature"
                        ),
                        createEntitlement(
                                "HEALTH_MONITORING",
                                "Health monitoring"
                        )
                ),
                Instant.parse(
                        "2026-10-22T12:00:00Z"
                )
        );

        when(entitlementSetRepository.findBySubscriptionId(
                new SubscriptionId(subscriptionUuid)
        )).thenReturn(
                Optional.of(existing)
        );

        var result =
                entitlementSetCommandService.handle(
                        new UpdateEntitlementsCommand(
                                subscriptionUuid,
                                Set.of(
                                        new EntitlementDefinition(
                                                "REAL_TIME_LOCATION",
                                                "Real-time location",
                                                "Allows real-time location monitoring"
                                        )
                                )
                        )
                );

        assertTrue(
                result.isSuccess()
        );

        EntitlementSet updated =
                result.toOptional()
                        .orElseThrow();

        assertEquals(
                1,
                updated.getEntitlements().size()
        );

        assertTrue(
                updated.contains(
                        new EntitlementCode(
                                "REAL_TIME_LOCATION"
                        )
                )
        );

        assertFalse(
                updated.contains(
                        new EntitlementCode(
                                "OLD_FEATURE"
                        )
                )
        );

        assertFalse(
                updated.contains(
                        new EntitlementCode(
                                "HEALTH_MONITORING"
                        )
                )
        );
    }

    @Test
    void shouldAllowReplacingEntitlementsWithEmptySet() {

        UUID subscriptionUuid =
                UUID.randomUUID();

        EntitlementSet existing =
                createEmptyEntitlementSet(
                        subscriptionUuid
                );

        existing.replaceWith(
                Set.of(
                        createEntitlement(
                                "REAL_TIME_LOCATION",
                                "Real-time location"
                        )
                ),
                Instant.parse(
                        "2026-10-22T12:00:00Z"
                )
        );

        when(entitlementSetRepository.findBySubscriptionId(
                new SubscriptionId(subscriptionUuid)
        )).thenReturn(
                Optional.of(existing)
        );

        var result =
                entitlementSetCommandService.handle(
                        new UpdateEntitlementsCommand(
                                subscriptionUuid,
                                Set.of()
                        )
                );

        assertTrue(
                result.isSuccess()
        );

        EntitlementSet updated =
                result.toOptional()
                        .orElseThrow();

        assertTrue(
                updated
                        .getEntitlements()
                        .isEmpty()
        );

        verify(entitlementSetRepository)
                .save(existing);
    }

    @Test
    void shouldRevokeAllEntitlements() {

        UUID subscriptionUuid =
                UUID.randomUUID();

        EntitlementSet existing =
                createEmptyEntitlementSet(
                        subscriptionUuid
                );

        existing.replaceWith(
                Set.of(
                        createEntitlement(
                                "REAL_TIME_LOCATION",
                                "Real-time location"
                        ),
                        createEntitlement(
                                "HEALTH_MONITORING",
                                "Health monitoring"
                        )
                ),
                Instant.parse(
                        "2026-10-22T12:00:00Z"
                )
        );

        when(entitlementSetRepository.findBySubscriptionId(
                new SubscriptionId(subscriptionUuid)
        )).thenReturn(
                Optional.of(existing)
        );

        var result =
                entitlementSetCommandService.handle(
                        new RevokeAllEntitlementsCommand(
                                subscriptionUuid
                        )
                );

        assertTrue(
                result.isSuccess()
        );

        EntitlementSet updated =
                result.toOptional()
                        .orElseThrow();

        assertTrue(
                updated
                        .getEntitlements()
                        .isEmpty()
        );

        assertNotNull(
                updated.getUpdatedAt()
        );

        verify(entitlementSetRepository)
                .save(existing);
    }

    @Test
    void shouldReturnFailureWhenEntitlementSetDoesNotExist() {

        UUID subscriptionUuid =
                UUID.randomUUID();

        when(entitlementSetRepository.findBySubscriptionId(
                new SubscriptionId(subscriptionUuid)
        )).thenReturn(
                Optional.empty()
        );

        var result =
                entitlementSetCommandService.handle(
                        new RevokeAllEntitlementsCommand(
                                subscriptionUuid
                        )
                );

        assertTrue(
                result.isFailure()
        );

        assertTrue(
                result.toOptional().isEmpty()
        );

        verify(
                entitlementSetRepository,
                never()
        ).save(
                any(EntitlementSet.class)
        );
    }

    private EntitlementSet createEmptyEntitlementSet(
            UUID subscriptionUuid
    ) {

        EntitlementSet entitlementSet =
                new EntitlementSet();

        entitlementSet.setSubscriptionId(
                new SubscriptionId(
                        subscriptionUuid
                )
        );

        entitlementSet.setEntitlements(
                Set.of()
        );

        entitlementSet.setUpdatedAt(
                Instant.parse(
                        "2026-10-22T10:00:00Z"
                )
        );

        return entitlementSet;
    }

    private Entitlement createEntitlement(
            String code,
            String name
    ) {

        Entitlement entitlement =
                new Entitlement();

        entitlement.setCode(
                new EntitlementCode(
                        code
                )
        );

        entitlement.setName(
                name
        );

        entitlement.setDescription(
                name + " capability"
        );

        return entitlement;
    }
}