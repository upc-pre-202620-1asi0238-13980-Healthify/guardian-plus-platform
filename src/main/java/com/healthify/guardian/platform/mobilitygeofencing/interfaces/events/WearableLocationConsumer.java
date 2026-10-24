package com.healthify.guardian.platform.mobilitygeofencing.interfaces.events;

import com.healthify.guardian.platform.mobilitygeofencing.application.ports.inbound.WearableLocationInputPort;
import com.healthify.guardian.platform.mobilitygeofencing.interfaces.transform.WearableLocationTransformer;
import org.springframework.stereotype.Component;

@Component
public class WearableLocationConsumer {

    private final WearableLocationInputPort wearableLocationInputPort;

    public WearableLocationConsumer(WearableLocationInputPort wearableLocationInputPort) {
        this.wearableLocationInputPort = wearableLocationInputPort;
    }

    public void consumeLocation(WearableLocationMessage message) {
        var command = WearableLocationTransformer.toCommand(message);
        wearableLocationInputPort.receiveLocation(command);
    }
}
