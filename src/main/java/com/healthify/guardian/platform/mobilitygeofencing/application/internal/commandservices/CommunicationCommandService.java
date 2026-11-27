package com.healthify.guardian.platform.mobilitygeofencing.application.internal.commandservices;

import com.healthify.guardian.platform.mobilitygeofencing.application.internal.queryservices.CommunicationQueryService;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.aggregates.CallSession;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.entities.CallLogEntry;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.repositories.CallLogRepository;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.repositories.CallSessionRepository;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.CallStatus;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.CallType;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.FragileCitizenId;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.WristbandCapabilityProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class CommunicationCommandService {

    private static final Logger log = LoggerFactory.getLogger(CommunicationCommandService.class);

    private final CallSessionRepository callSessionRepository;
    private final CallLogRepository callLogRepository;
    private final CommunicationQueryService communicationQueryService;

    public CommunicationCommandService(
            CallSessionRepository callSessionRepository,
            CallLogRepository callLogRepository,
            CommunicationQueryService communicationQueryService
    ) {
        this.callSessionRepository = callSessionRepository;
        this.callLogRepository = callLogRepository;
        this.communicationQueryService = communicationQueryService;
    }

    public CallSession startCall(FragileCitizenId fragileCitizenId, CallType requestedType) {
        WristbandCapabilityProfile capabilityProfile = communicationQueryService.getDeviceCapabilities(fragileCitizenId);
        CallSession session = CallSession.initiate(fragileCitizenId, requestedType, capabilityProfile);
        CallSession savedSession = callSessionRepository.save(session);

        if (savedSession.getStatus() == CallStatus.UNSUPPORTED) {
            callLogRepository.save(CallLogEntry.fromCallSession(savedSession));
        }

        return savedSession;
    }

    public Optional<CallSession> endCall(UUID callId) {
        return callSessionRepository.findById(callId)
                .map(session -> {
                    session.endCall();
                    CallSession updated = callSessionRepository.save(session);
                    callLogRepository.save(CallLogEntry.fromCallSession(updated));
                    return updated;
                });
    }

    public List<CallLogEntry> registerUnansweredCall(
            FragileCitizenId fragileCitizenId,
            CallType attemptedType,
            String reason
    ) {
        CallLogEntry entry = CallLogEntry.unanswered(fragileCitizenId, attemptedType, reason);
        callLogRepository.save(entry);
        return callLogRepository.findByFragileCitizenIdOrderByOccurredAtDesc(fragileCitizenId);
    }

    public void sendHapticAlert(FragileCitizenId fragileCitizenId, String message) {
        String alertPayload = (message != null && !message.isBlank())
                ? message
                : "Zumbido de alerta preventiva (vibración en muñeca)";
        log.info("Enviando alerta vibratoria háptica al wearable del ciudadano frágil [{}]: {}",
                fragileCitizenId.value(), alertPayload);
    }
}
