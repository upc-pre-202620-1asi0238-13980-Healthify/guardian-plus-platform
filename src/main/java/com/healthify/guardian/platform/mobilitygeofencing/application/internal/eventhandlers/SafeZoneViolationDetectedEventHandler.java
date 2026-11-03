package com.healthify.guardian.platform.mobilitygeofencing.application.internal.eventhandlers;

import com.healthify.guardian.platform.mobilitygeofencing.domain.model.events.SafeZoneViolationDetectedEvent;
import com.healthify.guardian.platform.mobilitygeofencing.interfaces.events.SafeZoneViolationIntegrationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Republishes the internal {@link SafeZoneViolationDetectedEvent} as a {@link SafeZoneViolationIntegrationEvent}
 * for consumption by {@code Emergency & Alerting}.
 */
@Service
public class SafeZoneViolationDetectedEventHandler {

    private final ApplicationEventPublisher eventPublisher;

    public SafeZoneViolationDetectedEventHandler(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    public void on(SafeZoneViolationDetectedEvent event) {
        var coordinates = event.location().coordinates();
        eventPublisher.publishEvent(new SafeZoneViolationIntegrationEvent(
                event.fragileCitizenId().value(),
                event.safeZoneId().value(),
                coordinates.latitude(),
                coordinates.longitude(),
                event.detectedAt()));
    }
}
