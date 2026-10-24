package com.healthify.guardian.platform.subscriptions.domain.model.aggregates;

import com.healthify.guardian.platform.subscriptions.domain.model.entities.Entitlement;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.EntitlementCode;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriptionId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntitlementSetTest {

    private static final SubscriptionId SUBSCRIPTION_ID =
            new SubscriptionId(
                    UUID.fromString(
                            "11111111-1111-1111-1111-111111111111"));

    private static final Instant NOW =
            Instant.parse(
                    "2026-10-23T13:00:00Z");

    @Test
    void shouldGrantAndRevokeEntitlement() {

        var healthCode =
                new EntitlementCode(
                        "HEALTH_MONITORING");

        var health =
                new Entitlement(
                        healthCode,
                        "Monitoreo de salud",
                        "Acceso al monitoreo de salud");

        var entitlementSet =
                new EntitlementSet(
                        SUBSCRIPTION_ID,
                        Set.of(),
                        NOW);

        entitlementSet.grant(
                health,
                NOW.plusSeconds(60));

        assertTrue(
                entitlementSet.contains(
                        healthCode));

        entitlementSet.revoke(
                healthCode,
                NOW.plusSeconds(120));

        assertFalse(
                entitlementSet.contains(
                        healthCode));
    }

    @Test
    void shouldRevokeAllEntitlements() {

        var code =
                new EntitlementCode(
                        "EMERGENCY_ALERTS");

        var entitlement =
                new Entitlement(
                        code,
                        "Alertas",
                        "Acceso a alertas");

        var entitlementSet =
                new EntitlementSet(
                        SUBSCRIPTION_ID,
                        Set.of(entitlement),
                        NOW);

        entitlementSet.revokeAll(
                NOW.plusSeconds(60));

        assertTrue(
                entitlementSet
                        .getEntitlements()
                        .isEmpty());
    }
}