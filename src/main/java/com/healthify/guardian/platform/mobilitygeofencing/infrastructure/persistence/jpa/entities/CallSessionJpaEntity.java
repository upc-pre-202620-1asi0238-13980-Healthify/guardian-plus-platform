package com.healthify.guardian.platform.mobilitygeofencing.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "call_sessions")
public class CallSessionJpaEntity {

    @Id
    private UUID id;

    @Column(name = "fragile_citizen_id", nullable = false)
    private UUID fragileCitizenId;

    @Column(name = "requested_type", nullable = false)
    private String requestedType;

    @Column(name = "active_type", nullable = false)
    private String activeType;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private boolean downgraded;

    @Column(name = "reason_message", length = 500)
    private String reasonMessage;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "duration_seconds")
    private Long durationSeconds;

    public CallSessionJpaEntity() {}

    public CallSessionJpaEntity(
            UUID id,
            UUID fragileCitizenId,
            String requestedType,
            String activeType,
            String status,
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

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getFragileCitizenId() { return fragileCitizenId; }
    public void setFragileCitizenId(UUID fragileCitizenId) { this.fragileCitizenId = fragileCitizenId; }
    public String getRequestedType() { return requestedType; }
    public void setRequestedType(String requestedType) { this.requestedType = requestedType; }
    public String getActiveType() { return activeType; }
    public void setActiveType(String activeType) { this.activeType = activeType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isDowngraded() { return downgraded; }
    public void setDowngraded(boolean downgraded) { this.downgraded = downgraded; }
    public String getReasonMessage() { return reasonMessage; }
    public void setReasonMessage(String reasonMessage) { this.reasonMessage = reasonMessage; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getEndedAt() { return endedAt; }
    public void setEndedAt(Instant endedAt) { this.endedAt = endedAt; }
    public Long getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Long durationSeconds) { this.durationSeconds = durationSeconds; }
}
