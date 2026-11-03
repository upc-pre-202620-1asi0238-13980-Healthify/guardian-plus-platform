package com.healthify.guardian.platform.careroutineswellness.application.internal.eventhandlers;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.ActivityLogEntry;
import com.healthify.guardian.platform.careroutineswellness.domain.model.events.ProlongedInactivityDetectedEvent;
import com.healthify.guardian.platform.careroutineswellness.domain.repositories.ActivityLogEntryRepository;
import com.healthify.guardian.platform.careroutineswellness.interfaces.events.ProlongedInactivityDetectedIntegrationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Reacts to the internal {@link ProlongedInactivityDetectedEvent}: records it in the person's recent
 * activity log and republishes it as a {@link ProlongedInactivityDetectedIntegrationEvent} for consumption
 * by {@code Emergency & Alerting}.
 *
 * <p>A failure to log never prevents the alert from being raised.</p>
 */
@Slf4j
@Service
public class ProlongedInactivityDetectedEventHandler {

    private final ApplicationEventPublisher eventPublisher;
    private final ActivityLogEntryRepository activityLogEntryRepository;

    public ProlongedInactivityDetectedEventHandler(
            ApplicationEventPublisher eventPublisher, ActivityLogEntryRepository activityLogEntryRepository) {
        this.eventPublisher = eventPublisher;
        this.activityLogEntryRepository = activityLogEntryRepository;
    }

    @EventListener
    public void on(ProlongedInactivityDetectedEvent event) {
        try {
            activityLogEntryRepository.save(ActivityLogEntry.from(event));
        } catch (RuntimeException e) {
            log.error("Prolonged inactivity of {} could not be logged", event.personUnderCareId().value(), e);
        }
        eventPublisher.publishEvent(new ProlongedInactivityDetectedIntegrationEvent(
                event.personUnderCareId().value(), event.detectedAt()));
    }
}
