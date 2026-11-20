package com.healthify.guardian.platform.emergencyalerting.application.queryservices;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.Incident;
import com.healthify.guardian.platform.emergencyalerting.domain.model.queries.GetIncidentByAlertIdQuery;
import com.healthify.guardian.platform.emergencyalerting.domain.model.queries.GetIncidentByIdQuery;
import com.healthify.guardian.platform.emergencyalerting.domain.model.queries.GetIncidentsQuery;
import org.springframework.data.domain.Page;

import java.util.Optional;

/**
 * Application service contract for queries over the {@code Incident} aggregate.
 */
public interface IncidentQueryService {

    Optional<Incident> handle(GetIncidentByIdQuery query);

    Optional<Incident> handle(GetIncidentByAlertIdQuery query);

    Page<Incident> handle(GetIncidentsQuery query);
}