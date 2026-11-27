package com.healthify.guardian.platform.careroutineswellness.application.queryservices;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.HydrationPlan;
import com.healthify.guardian.platform.careroutineswellness.domain.model.queries.GetHydrationPlanByPersonUnderCareIdQuery;
import com.healthify.guardian.platform.careroutineswellness.domain.model.queries.GetHydrationProgressQuery;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.HydrationProgress;

import java.util.Optional;

/**
 * Application service contract for hydration plan read queries.
 */
public interface HydrationPlanQueryService {

    /**
     * Handles retrieval of the hydration plan of a person under care.
     *
     * @param query person-under-care-id query
     * @return matching hydration plan, if one has been configured
     */
    Optional<HydrationPlan> handle(GetHydrationPlanByPersonUnderCareIdQuery query);

    /**
     * Handles the measurement of a person under care's hydration on a given day.
     *
     * @param query person and day
     * @return glasses consumed that day and when the next hydration reminder is due
     */
    HydrationProgress handle(GetHydrationProgressQuery query);
}
