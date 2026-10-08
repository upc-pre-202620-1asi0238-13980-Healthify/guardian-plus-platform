package com.healthify.guardian.platform.careroutineswellness.application.internal.eventhandlers;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.ActivityLogEntry;
import com.healthify.guardian.platform.careroutineswellness.domain.model.events.MovementDetectedEvent;
import com.healthify.guardian.platform.careroutineswellness.domain.repositories.ActivityLogEntryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Reacts to {@link MovementDetectedEvent} by recording, in the person's recent activity log, that the
 * inactivity counter was reset without an alarm.
 *
 * <p>Never lets a failure propagate back to the publisher: the activity monitor stays updated.</p>
 */
@Slf4j
@Service
public class MovementDetectedEventHandler {

    private final ActivityLogEntryRepository activityLogEntryRepository;

    public MovementDetectedEventHandler(ActivityLogEntryRepository activityLogEntryRepository) {
        this.activityLogEntryRepository = activityLogEntryRepository;
    }

    @EventListener
    public void on(MovementDetectedEvent event) {
        try {
            activityLogEntryRepository.save(ActivityLogEntry.from(event));
        } catch (RuntimeException e) {
            log.error("Movement of {} could not be logged", event.personUnderCareId().value(), e);
        }
    }
}
