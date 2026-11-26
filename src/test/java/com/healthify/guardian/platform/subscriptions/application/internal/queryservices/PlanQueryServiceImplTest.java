package com.healthify.guardian.platform.subscriptions.application.internal.queryservices;

import com.healthify.guardian.platform.subscriptions.domain.model.entities.Plan;
import com.healthify.guardian.platform.subscriptions.domain.model.queries.GetActivePlansQuery;
import com.healthify.guardian.platform.subscriptions.domain.model.queries.GetPlanByIdQuery;
import com.healthify.guardian.platform.subscriptions.domain.repositories.PlanRepository;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.BillingCycle;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.Money;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.PlanId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlanQueryServiceImplTest {

    private PlanRepository planRepository;
    private PlanQueryServiceImpl service;

    @BeforeEach
    void setUp() {

        planRepository =
                mock(PlanRepository.class);

        service =
                new PlanQueryServiceImpl(
                        planRepository
                );
    }

    @Test
    void shouldReturnAllActivePlans() {

        var basic = createPlan(
                "Guardian Basic",
                "0.00"
        );

        var premium = createPlan(
                "Guardian Premium",
                "29.90"
        );

        var pro = createPlan(
                "Cuidado Pro",
                "59.90"
        );

        when(planRepository.findActivePlans())
                .thenReturn(
                        List.of(basic, premium, pro)
                );

        var result = service.handle(
                new GetActivePlansQuery()
        );

        assertEquals(
                List.of(basic, premium, pro),
                result
        );

        verify(planRepository)
                .findActivePlans();
    }

    @Test
    void shouldReturnEmptyListWhenNoActivePlansExist() {

        when(planRepository.findActivePlans())
                .thenReturn(List.of());

        var result = service.handle(
                new GetActivePlansQuery()
        );

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(planRepository)
                .findActivePlans();
    }

    @Test
    void shouldPreserveExistingPlanByIdQuery() {

        var plan = createPlan(
                "Guardian Premium",
                "29.90"
        );

        when(planRepository.findById(plan.getId()))
                .thenReturn(Optional.of(plan));

        var result = service.handle(
                new GetPlanByIdQuery(plan.getId())
        );

        assertTrue(result.isPresent());
        assertEquals(plan, result.orElseThrow());

        verify(planRepository)
                .findById(plan.getId());
    }

    @Test
    void shouldReturnEmptyWhenPlanDoesNotExist() {

        var planId =
                new PlanId(UUID.randomUUID());

        when(planRepository.findById(planId))
                .thenReturn(Optional.empty());

        var result = service.handle(
                new GetPlanByIdQuery(planId)
        );

        assertTrue(result.isEmpty());

        verify(planRepository)
                .findById(planId);
    }

    private Plan createPlan(
            String name,
            String price
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
                Set.of()
        );
    }
}