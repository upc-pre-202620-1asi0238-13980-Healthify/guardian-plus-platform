package com.healthify.guardian.platform.mobilitygeofencing.application.ports.inbound;

import com.healthify.guardian.platform.mobilitygeofencing.domain.model.commands.ReceiveLocationCommand;

public interface WearableLocationInputPort {
    void receiveLocation(ReceiveLocationCommand command);
}
