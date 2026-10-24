package com.healthify.guardian.platform.emergencyalerting.domain.repositories;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.Incident;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.IncidentId;

import java.util.Optional;

/**
 * Incident aggregate repository port.
 */
public interface IncidentRepository {

    /**
     * Persists an incident (create or update) and publishes its registered domain events.
     *
     * @param incident the incident to save
     * @return the saved incident
     */
    Incident save(Incident incident);

    Optional<Incident> findById(IncidentId id);

    Optional<Incident> findByAlertId(AlertId alertId);

    boolean existsByAlertId(AlertId alertId);
}
