package com.healthify.guardian.platform.careroutineswellness.application.internal.eventhandlers;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.ActivityLogEntry;
import com.healthify.guardian.platform.careroutineswellness.domain.model.events.WalkDetectedEvent;
import com.healthify.guardian.platform.careroutineswellness.domain.repositories.ActivityLogEntryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Reacts to {@link WalkDetectedEvent} by recording the finished walk in the person's recent activity log.
 *
 * <p>Never lets a failure propagate back to the publisher: the activity monitor stays updated.</p>
 */
@Slf4j
@Service
public class WalkDetectedEventHandler {

    private final ActivityLogEntryRepository activityLogEntryRepository;

    public WalkDetectedEventHandler(ActivityLogEntryRepository activityLogEntryRepository) {
        this.activityLogEntryRepository = activityLogEntryRepository;
    }

    @EventListener
    public void on(WalkDetectedEvent event) {
        try {
            activityLogEntryRepository.save(ActivityLogEntry.from(event));
        } catch (RuntimeException e) {
            log.error("Walk of {} could not be logged", event.personUnderCareId().value(), e);
        }
    }
}
