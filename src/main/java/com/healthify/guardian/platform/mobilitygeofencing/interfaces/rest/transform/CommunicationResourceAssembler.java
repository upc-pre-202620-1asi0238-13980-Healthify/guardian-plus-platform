package com.healthify.guardian.platform.mobilitygeofencing.interfaces.rest.transform;

import com.healthify.guardian.platform.mobilitygeofencing.domain.model.aggregates.CallSession;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.entities.CallLogEntry;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.FragileCitizenId;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.WristbandCapabilityProfile;
import com.healthify.guardian.platform.mobilitygeofencing.interfaces.rest.resources.CallLogEntryResource;
import com.healthify.guardian.platform.mobilitygeofencing.interfaces.rest.resources.CallSessionResource;
import com.healthify.guardian.platform.mobilitygeofencing.interfaces.rest.resources.DeviceCapabilitiesResource;

public class CommunicationResourceAssembler {

    public static DeviceCapabilitiesResource toDeviceCapabilitiesResource(
            FragileCitizenId fragileCitizenId,
            WristbandCapabilityProfile profile
    ) {
        return new DeviceCapabilitiesResource(
                fragileCitizenId.value(),
                profile.wristbandModel(),
                profile.hasCamera(),
                profile.hasScreen(),
                profile.hasMicrophone(),
                profile.hasSpeaker(),
                profile.supportsHapticAlerts(),
                profile.supportsTelemetry(),
                profile.citizenPhone(),
                profile.caregiverPhone()
        );
    }

    public static CallSessionResource toCallSessionResource(CallSession session) {
        return new CallSessionResource(
                session.getId(),
                session.getFragileCitizenId().value(),
                session.getActiveType().name(),
                session.getStatus().name(),
                session.isDowngraded(),
                session.getReasonMessage(),
                session.getStartedAt()
        );
    }

    public static CallLogEntryResource toCallLogEntryResource(CallLogEntry entry) {
        return new CallLogEntryResource(
                entry.getId(),
                entry.getFragileCitizenId().value(),
                entry.getCallType().name(),
                entry.getStatus().name(),
                entry.isDowngraded(),
                entry.getReason(),
                entry.getDurationSeconds(),
                entry.getOccurredAt()
        );
    }
}
