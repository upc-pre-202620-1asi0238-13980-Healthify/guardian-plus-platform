package com.healthify.guardian.platform.mobilitygeofencing.interfaces.transform;

import com.healthify.guardian.platform.mobilitygeofencing.domain.model.commands.ReceiveLocationCommand;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.Coordinates;
import com.healthify.guardian.platform.mobilitygeofencing.interfaces.events.WearableLocationMessage;

public class WearableLocationTransformer {

    public static ReceiveLocationCommand toCommand(WearableLocationMessage message) {
        Coordinates coordinates = new Coordinates(message.latitude(), message.longitude());
        return new ReceiveLocationCommand(
                message.fragileCitizenId(),
                coordinates,
                message.accuracyInMeters(),
                message.recordedAt()
        );
    }
}
