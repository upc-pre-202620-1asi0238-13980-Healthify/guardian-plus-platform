package com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates;

import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.OpenIncidentCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.events.IncidentClosedEvent;
import com.healthify.guardian.platform.emergencyalerting.domain.model.events.IncidentOpenedEvent;
import com.healthify.guardian.platform.emergencyalerting.domain.model.events.IncidentStabilizedEvent;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.IncidentId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.IncidentStatus;
import com.healthify.guardian.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.time.Instant;

/**
 * Aggregate root representing the human attention given to an acknowledged alert, from the moment
 * a Care Circle member takes it on until it is closed ({@code IN_ATTENTION → STABILIZED → CLOSED}).
 *
 * <p>References its alert by identity only; an alert originates at most one incident, and alerts
 * dismissed as false positives never originate one.</p>
 */
@Getter
public class Incident extends AbstractDomainAggregateRoot<Incident> {

    private static final String MARKED_IN_ATTENTION_AT_INVALID_MESSAGE_KEY = "incident.marked-in-attention-at.invalid";
    private static final String CANNOT_STABILIZE_MESSAGE_KEY = "incident.cannot.stabilize";
    private static final String CANNOT_CLOSE_MESSAGE_KEY = "incident.cannot.close";

    private IncidentId id;
    private AlertId alertId;
    private IncidentStatus status;
    private Instant markedInAttentionAt;
    private Instant stabilizedAt;
    private Instant closedAt;
    private String notes;

    /** Reconstitution constructor, used by the persistence assembler. */
    public Incident() {
    }

    /** Opens a new incident in {@code IN_ATTENTION} status for an acknowledged alert. */
    public Incident(OpenIncidentCommand command) {
        if (command.markedInAttentionAt() == null) {
            throw new IllegalArgumentException(MARKED_IN_ATTENTION_AT_INVALID_MESSAGE_KEY);
        }
        this.id = IncidentId.generate();
        this.alertId = new AlertId(command.alertId());
        this.status = IncidentStatus.IN_ATTENTION;
        this.markedInAttentionAt = command.markedInAttentionAt();
        registerDomainEvent(IncidentOpenedEvent.from(this));
    }

    /** Declares the Fragile Citizen's situation stabilized. */
    public void stabilize(String notes, Instant now) {
        if (status != IncidentStatus.IN_ATTENTION) {
            throw new IllegalStateException(CANNOT_STABILIZE_MESSAGE_KEY);
        }
        this.status = IncidentStatus.STABILIZED;
        this.stabilizedAt = now;
        appendNotes(notes);
        registerDomainEvent(IncidentStabilizedEvent.from(this));
    }

    /**
     * Definitively closes this incident. It may be closed straight from {@code IN_ATTENTION} when
     * there was nothing to stabilize (e.g. a fall without injuries).
     */
    public void close(String notes, Instant now) {
        if (isClosed()) {
            throw new IllegalStateException(CANNOT_CLOSE_MESSAGE_KEY);
        }
        this.status = IncidentStatus.CLOSED;
        this.closedAt = now;
        appendNotes(notes);
        registerDomainEvent(IncidentClosedEvent.from(this));
    }

    public boolean isClosed() {
        return status == IncidentStatus.CLOSED;
    }

    /** Keeps the notes of every stage instead of overwriting them, one stage per line. */
    private void appendNotes(String newNotes) {
        if (newNotes == null || newNotes.isBlank()) {
            return;
        }
        this.notes = this.notes == null ? newNotes.strip() : this.notes + System.lineSeparator() + newNotes.strip();
    }

    /** Restores an identity and state from persistence. Used by the persistence assembler. */
    public void setId(IncidentId id) {
        this.id = id;
    }

    public void setAlertId(AlertId alertId) {
        this.alertId = alertId;
    }

    public void setStatus(IncidentStatus status) {
        this.status = status;
    }

    public void setMarkedInAttentionAt(Instant markedInAttentionAt) {
        this.markedInAttentionAt = markedInAttentionAt;
    }

    public void setStabilizedAt(Instant stabilizedAt) {
        this.stabilizedAt = stabilizedAt;
    }

    public void setClosedAt(Instant closedAt) {
        this.closedAt = closedAt;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
