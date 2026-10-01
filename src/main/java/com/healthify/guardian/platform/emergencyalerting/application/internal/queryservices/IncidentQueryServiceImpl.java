package com.healthify.guardian.platform.emergencyalerting.application.internal.queryservices;

import com.healthify.guardian.platform.emergencyalerting.application.queryservices.IncidentQueryService;
import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.Incident;
import com.healthify.guardian.platform.emergencyalerting.domain.model.queries.GetIncidentByAlertIdQuery;
import com.healthify.guardian.platform.emergencyalerting.domain.model.queries.GetIncidentByIdQuery;
import com.healthify.guardian.platform.emergencyalerting.domain.repositories.IncidentRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Application service that answers incident queries.
 */
@Service
public class IncidentQueryServiceImpl implements IncidentQueryService {

    private final IncidentRepository incidentRepository;

    public IncidentQueryServiceImpl(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    @Override
    public Optional<Incident> handle(GetIncidentByIdQuery query) {
        return incidentRepository.findById(query.incidentId());
    }

    @Override
    public Optional<Incident> handle(GetIncidentByAlertIdQuery query) {
        return incidentRepository.findByAlertId(query.alertId());
    }
}
