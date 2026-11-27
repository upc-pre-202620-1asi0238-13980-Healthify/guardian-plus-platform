package com.healthify.guardian.platform.mobilitygeofencing.domain.model.aggregates;

import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.CallStatus;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.CallType;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.FragileCitizenId;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.WristbandCapabilityProfile;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public class CallSession {
    private final UUID id;
    private final FragileCitizenId fragileCitizenId;
    private final CallType requestedType;
    private final CallType activeType;
    private CallStatus status;
    private final boolean downgraded;
    private String reasonMessage;
    private final Instant startedAt;
    private Instant endedAt;
    private Long durationSeconds;

    public CallSession(
            UUID id,
            FragileCitizenId fragileCitizenId,
            CallType requestedType,
            CallType activeType,
            CallStatus status,
            boolean downgraded,
            String reasonMessage,
            Instant startedAt,
            Instant endedAt,
            Long durationSeconds
    ) {
        this.id = id;
        this.fragileCitizenId = fragileCitizenId;
        this.requestedType = requestedType;
        this.activeType = activeType;
        this.status = status;
        this.downgraded = downgraded;
        this.reasonMessage = reasonMessage;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.durationSeconds = durationSeconds;
    }

    public static CallSession initiate(
            FragileCitizenId fragileCitizenId,
            CallType requestedType,
            WristbandCapabilityProfile capabilityProfile
    ) {
        UUID callId = UUID.randomUUID();
        Instant now = Instant.now();

        if (!capabilityProfile.supportsVoiceCall() && !capabilityProfile.supportsVideoCall()) {
            String message = String.format(
                    "El modelo %s solo soporta telemetría y alertas hápticas. Use los números de respaldo: Ciudadano (%s), Cuidador (%s).",
                    capabilityProfile.wristbandModel(),
                    capabilityProfile.citizenPhone(),
                    capabilityProfile.caregiverPhone()
            );
            return new CallSession(
                    callId,
                    fragileCitizenId,
                    requestedType,
                    requestedType,
                    CallStatus.UNSUPPORTED,
                    false,
                    message,
                    now,
                    now,
                    0L
            );
        }

        if (requestedType == CallType.VIDEO && !capabilityProfile.supportsVideoCall()) {
            String message = String.format(
                    "El modelo %s no cuenta con cámara de vídeo. La sesión se degradó automáticamente a llamada de voz bidireccional (VOICE).",
                    capabilityProfile.wristbandModel()
            );
            return new CallSession(
                    callId,
                    fragileCitizenId,
                    requestedType,
                    CallType.VOICE,
                    CallStatus.ACTIVE,
                    true,
                    message,
                    now,
                    null,
                    0L
            );
        }

        CallType effectiveType = (requestedType == CallType.VIDEO && capabilityProfile.supportsVideoCall())
                ? CallType.VIDEO
                : CallType.VOICE;

        String message = effectiveType == CallType.VIDEO
                ? "Canal de videollamada bidireccional establecido exitosamente."
                : "Canal de voz bidireccional establecido exitosamente.";

        return new CallSession(
                callId,
                fragileCitizenId,
                requestedType,
                effectiveType,
                CallStatus.ACTIVE,
                false,
                message,
                now,
                null,
                0L
        );
    }

    public void endCall() {
        if (this.status == CallStatus.ACTIVE) {
            this.status = CallStatus.ENDED;
            this.endedAt = Instant.now();
            this.durationSeconds = Math.max(0L, Duration.between(this.startedAt, this.endedAt).getSeconds());
            this.reasonMessage = "Llamada finalizada correctamente.";
        }
    }

    public UUID getId() { return id; }
    public FragileCitizenId getFragileCitizenId() { return fragileCitizenId; }
    public CallType getRequestedType() { return requestedType; }
    public CallType getActiveType() { return activeType; }
    public CallStatus getStatus() { return status; }
    public boolean isDowngraded() { return downgraded; }
    public String getReasonMessage() { return reasonMessage; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getEndedAt() { return endedAt; }
    public Long getDurationSeconds() { return durationSeconds; }
}
