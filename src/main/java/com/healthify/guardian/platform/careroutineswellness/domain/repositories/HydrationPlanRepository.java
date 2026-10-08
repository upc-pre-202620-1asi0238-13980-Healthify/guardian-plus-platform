package com.healthify.guardian.platform.careroutineswellness.domain.repositories;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.HydrationPlan;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;

import java.util.Optional;

/**
 * Hydration plan aggregate repository port.
 */
public interface HydrationPlanRepository {

    /**
     * Retrieves the single hydration plan of a person under care.
     *
     * @param personUnderCareId the person whose hydration plan is requested
     * @return the matching hydration plan, if one has been configured yet
     */
    Optional<HydrationPlan> findByPersonUnderCareId(PersonUnderCareId personUnderCareId);

    /**
     * Persists a hydration plan (create or update) and publishes its registered domain events.
     *
     * @param plan the hydration plan to save
     * @return the saved hydration plan
     */
    HydrationPlan save(HydrationPlan plan);
}
