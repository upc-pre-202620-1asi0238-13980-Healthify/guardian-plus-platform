package com.healthify.guardian.platform.subscriptions.interfaces.rest;

import com.healthify.guardian.platform.subscriptions.application.queryservices.PlanQueryService;
import com.healthify.guardian.platform.subscriptions.domain.model.entities.Plan;
import com.healthify.guardian.platform.subscriptions.domain.model.queries.GetActivePlansQuery;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.BillingCycle;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.EntitlementCode;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.Money;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.PlanId;
import com.healthify.guardian.platform.subscriptions.interfaces.rest.resources.PlanResource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlanControllerTest {

    private PlanQueryService planQueryService;
    private PlanController controller;

    @BeforeEach
    void setUp() {

        planQueryService =
                mock(PlanQueryService.class);

        controller =
                new PlanController(planQueryService);
    }

    @Test
    void shouldReturnActivePlansOrderedByPrice() {

        var basic = createPlan(
                "Guardian Basic",
                "0.00",
                Set.of(
                        new EntitlementCode("EMERGENCY_ALERTS")
                )
        );

        var premium = createPlan(
                "Guardian Premium",
                "29.90",
                Set.of(
                        new EntitlementCode("EMERGENCY_ALERTS"),
                        new EntitlementCode("HEALTH_MONITORING")
                )
        );

        var pro = createPlan(
                "Cuidado Pro",
                "59.90",
                Set.of(
                        new EntitlementCode("EMERGENCY_ALERTS"),
                        new EntitlementCode("HEALTH_MONITORING"),
                        new EntitlementCode("LOCATION_HISTORY")
                )
        );

        when(
                planQueryService.handle(
                        new GetActivePlansQuery()
                )
        ).thenReturn(
                List.of(pro, basic, premium)
        );

        var response = controller.getActivePlans();

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        var plans = response.getBody();

        assertNotNull(plans);
        assertEquals(3, plans.size());

        assertEquals(
                List.of(
                        "Guardian Basic",
                        "Guardian Premium",
                        "Cuidado Pro"
                ),
                plans.stream()
                        .map(PlanResource::name)
                        .toList()
        );

        assertEquals(
                new BigDecimal("0.00"),
                plans.get(0).price()
        );

        assertEquals(
                new BigDecimal("29.90"),
                plans.get(1).price()
        );

        assertEquals(
                new BigDecimal("59.90"),
                plans.get(2).price()
        );

        assertFalse(plans.get(0).requiresPayment());
        assertTrue(plans.get(1).requiresPayment());
        assertTrue(plans.get(2).requiresPayment());

        assertTrue(
                plans.stream()
                        .allMatch(plan ->
                                plan.active()
                                        && "PEN".equals(plan.currency())
                                        && "MONTHLY".equals(plan.billingCycle())
                        )
        );

        verify(planQueryService)
                .handle(new GetActivePlansQuery());
    }

    @Test
    void shouldReturnPlanEntitlementCodes() {

        var plan = createPlan(
                "Guardian Premium",
                "29.90",
                Set.of(
                        new EntitlementCode("REAL_TIME_LOCATION"),
                        new EntitlementCode("HEALTH_MONITORING")
                )
        );

        when(
                planQueryService.handle(
                        new GetActivePlansQuery()
                )
        ).thenReturn(List.of(plan));

        var response = controller.getActivePlans();

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        var plans = response.getBody();

        assertNotNull(plans);
        assertEquals(1, plans.size());

        var resource = plans.getFirst();

        assertEquals(
                plan.getId().value(),
                resource.id()
        );

        assertEquals(
                Set.of(
                        "REAL_TIME_LOCATION",
                        "HEALTH_MONITORING"
                ),
                resource.entitlementCodes()
        );
    }

    @Test
    void shouldReturnEmptyListWhenNoActivePlansExist() {

        when(
                planQueryService.handle(
                        new GetActivePlansQuery()
                )
        ).thenReturn(List.of());

        var response = controller.getActivePlans();

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        verify(planQueryService)
                .handle(new GetActivePlansQuery());
    }

    private Plan createPlan(
            String name,
            String price,
            Set<EntitlementCode> entitlementCodes
    ) {

        return new Plan(
                new PlanId(UUID.randomUUID()),
                name,
                new Money(
                        new BigDecimal(price),
                        Currency.getInstance("PEN")
                ),
                BillingCycle.MONTHLY,
                true,
                entitlementCodes
        );
    }
}