package com.healthify.guardian.platform.mobilitygeofencing.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "call_logs")
public class CallLogJpaEntity {

    @Id
    private UUID id;

    @Column(name = "fragile_citizen_id", nullable = false)
    private UUID fragileCitizenId;

    @Column(name = "call_type", nullable = false)
    private String callType;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private boolean downgraded;

    @Column(length = 500)
    private String reason;

    @Column(name = "duration_seconds")
    private Long durationSeconds;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    public CallLogJpaEntity() {}

    public CallLogJpaEntity(
            UUID id,
            UUID fragileCitizenId,
            String callType,
            String status,
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

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getFragileCitizenId() { return fragileCitizenId; }
    public void setFragileCitizenId(UUID fragileCitizenId) { this.fragileCitizenId = fragileCitizenId; }
    public String getCallType() { return callType; }
    public void setCallType(String callType) { this.callType = callType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isDowngraded() { return downgraded; }
    public void setDowngraded(boolean downgraded) { this.downgraded = downgraded; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Long getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Long durationSeconds) { this.durationSeconds = durationSeconds; }
    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }
}
