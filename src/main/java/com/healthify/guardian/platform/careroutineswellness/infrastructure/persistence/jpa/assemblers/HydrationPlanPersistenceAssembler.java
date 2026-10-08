package com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.assemblers;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.HydrationPlan;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.HydrationPlanId;
import com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.entities.HydrationPlanPersistenceEntity;

/**
 * Static assembler between the {@link HydrationPlan} domain aggregate and its persistence entity.
 */
public final class HydrationPlanPersistenceAssembler {

    private HydrationPlanPersistenceAssembler() {
    }

    public static HydrationPlan toDomainFromPersistence(HydrationPlanPersistenceEntity entity) {
        if (entity == null) return null;
        var plan = new HydrationPlan();
        plan.setId(new HydrationPlanId(entity.getId()));
        plan.setPersonUnderCareId(entity.getPersonUnderCareId());
        plan.setActive(entity.getActive());
        plan.setDailyGoalGlasses(entity.getDailyGoalGlasses());
        plan.setIntervalHours(entity.getIntervalHours());
        plan.setRespectSleepWindow(entity.getRespectSleepWindow());
        return plan;
    }

    public static HydrationPlanPersistenceEntity toPersistenceFromDomain(HydrationPlan plan) {
        if (plan == null) return null;
        var entity = new HydrationPlanPersistenceEntity();
        entity.setId(plan.getId().value());
        entity.setPersonUnderCareId(plan.getPersonUnderCareId());
        entity.setActive(plan.getActive());
        entity.setDailyGoalGlasses(plan.getDailyGoalGlasses());
        entity.setIntervalHours(plan.getIntervalHours());
        entity.setRespectSleepWindow(plan.getRespectSleepWindow());
        return entity;
    }
}
