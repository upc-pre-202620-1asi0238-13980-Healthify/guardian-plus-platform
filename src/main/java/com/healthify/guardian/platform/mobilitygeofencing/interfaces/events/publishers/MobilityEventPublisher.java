package com.healthify.guardian.platform.mobilitygeofencing.interfaces.events.publishers;

import com.healthify.guardian.platform.mobilitygeofencing.application.ports.inbound.MobilityEventOutputPort;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.events.SafeZoneViolationDetectedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class MobilityEventPublisher implements MobilityEventOutputPort {

    private final ApplicationEventPublisher eventPublisher;

    public MobilityEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void publish(SafeZoneViolationDetectedEvent event) {
        // Transmite el evento de dominio a través del bus de eventos/broker hacia Emergency & Alerting
        eventPublisher.publishEvent(event);
    }
}
