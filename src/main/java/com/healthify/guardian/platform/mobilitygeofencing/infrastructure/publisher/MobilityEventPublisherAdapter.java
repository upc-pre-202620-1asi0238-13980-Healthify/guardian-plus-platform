package com.healthify.guardian.platform.mobilitygeofencing.infrastructure.publisher;

import com.healthify.guardian.platform.mobilitygeofencing.application.ports.inbound.MobilityEventOutputPort;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.events.SafeZoneViolationDetectedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class MobilityEventPublisherAdapter implements MobilityEventOutputPort {

    private final ApplicationEventPublisher eventPublisher;

    public MobilityEventPublisherAdapter(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void publish(SafeZoneViolationDetectedEvent event) {
        // public event integred for Spring Application.
        eventPublisher.publishEvent(event);
    }
}
