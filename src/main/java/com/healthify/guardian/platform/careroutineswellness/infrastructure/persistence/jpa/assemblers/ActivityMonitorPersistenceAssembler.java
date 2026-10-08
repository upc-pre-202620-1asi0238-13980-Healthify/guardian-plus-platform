package com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.assemblers;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.ActivityMonitor;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ActivityMonitorId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.InactivityDetectionSettings;
import com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.entities.ActivityMonitorPersistenceEntity;

import java.math.BigDecimal;

/**
 * Static assembler between the {@link ActivityMonitor} domain aggregate and its persistence entity.
 *
 * <p>Rows written before detection was configurable get the default detection settings.</p>
 */
public final class ActivityMonitorPersistenceAssembler {

    private ActivityMonitorPersistenceAssembler() {
    }

    public static ActivityMonitor toDomainFromPersistence(ActivityMonitorPersistenceEntity entity) {
        if (entity == null) return null;
        var monitor = new ActivityMonitor();
        monitor.setId(new ActivityMonitorId(entity.getId()));
        monitor.setPersonUnderCareId(entity.getPersonUnderCareId());
        monitor.setStatus(entity.getStatus());
        monitor.setInactivitySince(entity.getInactivitySince());
        monitor.setDetectionSettings(entity.getInactivityThresholdMinutes() == null
                ? InactivityDetectionSettings.defaults()
                : new InactivityDetectionSettings(
                        !Boolean.FALSE.equals(entity.getDetectionEnabled()), entity.getInactivityThresholdMinutes()));
        monitor.setInactiveMinutes(entity.getInactiveMinutes() != null ? entity.getInactiveMinutes() : BigDecimal.ZERO);
        monitor.setLastMovementAt(entity.getLastMovementAt());
        monitor.setLastSampleAt(entity.getLastSampleAt());
        monitor.setActiveSince(entity.getActiveSince());
        monitor.setActiveStreakSteps(entity.getActiveStreakSteps() != null ? entity.getActiveStreakSteps() : 0);
        return monitor;
    }

    public static ActivityMonitorPersistenceEntity toPersistenceFromDomain(ActivityMonitor monitor) {
        if (monitor == null) return null;
        var entity = new ActivityMonitorPersistenceEntity();
        entity.setId(monitor.getId().value());
        entity.setPersonUnderCareId(monitor.getPersonUnderCareId());
        entity.setStatus(monitor.getStatus());
        entity.setInactivitySince(monitor.getInactivitySince());
        entity.setDetectionEnabled(monitor.getDetectionSettings().enabled());
        entity.setInactivityThresholdMinutes(monitor.getDetectionSettings().thresholdMinutes());
        entity.setInactiveMinutes(monitor.getInactiveMinutes());
        entity.setLastMovementAt(monitor.getLastMovementAt());
        entity.setLastSampleAt(monitor.getLastSampleAt());
        entity.setActiveSince(monitor.getActiveSince());
        entity.setActiveStreakSteps(monitor.getActiveStreakSteps());
        return entity;
    }
}
