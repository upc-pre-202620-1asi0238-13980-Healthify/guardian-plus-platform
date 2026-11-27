package com.healthify.guardian.platform.mobilitygeofencing.domain.model.entities;

import com.healthify.guardian.platform.mobilitygeofencing.domain.model.aggregates.CallSession;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.CallStatus;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.CallType;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.FragileCitizenId;

import java.time.Instant;
import java.util.UUID;

public class CallLogEntry {
    private final UUID id;
    private final FragileCitizenId fragileCitizenId;
    private final CallType callType;
    private final CallStatus status;
    private final boolean downgraded;
    private final String reason;
    private final Long durationSeconds;
    private final Instant occurredAt;

    public CallLogEntry(
            UUID id,
            FragileCitizenId fragileCitizenId,
            CallType callType,
            CallStatus status,
            boolean downgraded,
            String reason,
            Long durationSeconds,
            Instant occurredAt
    ) {
        this.id = id;
        this.fragileCitizenId = fragileCitizenId;
        this.callType = callType;
        this.status = status;
        this.downgraded = downgraded;
        this.reason = reason;
        this.durationSeconds = durationSeconds;
        this.occurredAt = occurredAt;
    }

    public static CallLogEntry fromCallSession(CallSession session) {
        return new CallLogEntry(
                UUID.randomUUID(),
                session.getFragileCitizenId(),
                session.getActiveType(),
                session.getStatus(),
                session.isDowngraded(),
                session.getReasonMessage(),
                session.getDurationSeconds() != null ? session.getDurationSeconds() : 0L,
                session.getEndedAt() != null ? session.getEndedAt() : session.getStartedAt()
        );
    }

    public static CallLogEntry unanswered(
            FragileCitizenId fragileCitizenId,
            CallType attemptedType,
            String reason
    ) {
        String effectiveReason = (reason != null && !reason.isBlank())
                ? reason
                : "No atendida tras 30 segundos de espera (timeout).";
        return new CallLogEntry(
                UUID.randomUUID(),
                fragileCitizenId,
                attemptedType,
                CallStatus.UNANSWERED,
                false,
                effectiveReason,
                0L,
                Instant.now()
        );
    }

    public UUID getId() { return id; }
    public FragileCitizenId getFragileCitizenId() { return fragileCitizenId; }
    public CallType getCallType() { return callType; }
    public CallStatus getStatus() { return status; }
    public boolean isDowngraded() { return downgraded; }
    public String getReason() { return reason; }
    public Long getDurationSeconds() { return durationSeconds; }
    public Instant getOccurredAt() { return occurredAt; }
}
