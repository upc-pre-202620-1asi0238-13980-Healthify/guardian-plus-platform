package com.healthify.guardian.platform.emergencyalerting.domain.repositories;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.Incident;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertSourceType;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.DateRange;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.IncidentId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.Severity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    /**
     * Pages through incidents using data from the alert that originated each
     * incident.
     *
     * @param patientId optional monitored person filter
     * @param period    optional alert triggering period
     * @param eventType optional alert source/event type
     * @param severity  optional clinical severity
     * @param pageable  requested page
     * @return the requested incident page, most recent first
     */
    Page<Incident> findAll(
            CareRecipientProfileId patientId,
            DateRange period,
            AlertSourceType eventType,
            Severity severity,
            Pageable pageable);

    boolean existsByAlertId(AlertId alertId);
}
