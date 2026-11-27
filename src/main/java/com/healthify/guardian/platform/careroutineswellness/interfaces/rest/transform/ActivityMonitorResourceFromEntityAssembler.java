package com.healthify.guardian.platform.careroutineswellness.interfaces.rest.transform;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.ActivityMonitor;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.SleepWindow;
import com.healthify.guardian.platform.careroutineswellness.interfaces.rest.resources.ActivityMonitorResource;

/**
 * Assembler that converts an {@link ActivityMonitor} domain aggregate into an {@link ActivityMonitorResource}.
 */
public final class ActivityMonitorResourceFromEntityAssembler {

    private ActivityMonitorResourceFromEntityAssembler() {
    }

    /**
     * @param sleepWindow the person's sleep window; inactivity is watched the rest of the day
     */
    public static ActivityMonitorResource toResourceFromEntity(ActivityMonitor monitor, SleepWindow sleepWindow) {
        var settings = monitor.getDetectionSettings();
        return new ActivityMonitorResource(
                monitor.getId().value(),
                monitor.getPersonUnderCareId().value(),
                monitor.getStatus().name(),
                monitor.getInactivitySince(),
                monitor.getInactiveMinutes(),
                monitor.getLastMovementAt(),
                monitor.getLastSampleAt(),
                settings.enabled(),
                settings.thresholdMinutes(),
                sleepWindow.end(),
                sleepWindow.start());
    }
}
