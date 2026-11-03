package com.healthify.guardian.platform.mobilitygeofencing.application.ports.inbound;

import com.healthify.guardian.platform.mobilitygeofencing.domain.model.events.SafeZoneViolationDetectedEvent;

public interface MobilityEventOutputPort {
    void publish(SafeZoneViolationDetectedEvent event);
}
