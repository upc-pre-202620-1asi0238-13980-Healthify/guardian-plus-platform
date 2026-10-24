package com.healthify.guardian.platform.profile.domain.model.aggregates;

import com.healthify.guardian.platform.profile.domain.model.commands.EstablishCareRelationshipCommand;
import com.healthify.guardian.platform.profile.domain.model.events.CareRelationshipEndedEvent;
import com.healthify.guardian.platform.profile.domain.model.events.CareRelationshipEstablishedEvent;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.CareRelationshipId;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.CareRelationshipStatus;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.RelationshipType;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.UserId;
import com.healthify.guardian.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.time.Instant;

/**
 * Aggregate root representing the responsibility relationship between
 * a Guardian+ user and a person under care.
 */
@Getter
public class CareRelationship extends AbstractDomainAggregateRoot<CareRelationship> {

    private static final String INVALID_RELATIONSHIP_TYPE_MESSAGE_KEY =
            "care-relationship.type.invalid";
    private static final String INVALID_TIMESTAMP_MESSAGE_KEY =
            "care-relationship.timestamp.invalid";
    private static final String ALREADY_ESTABLISHED_MESSAGE_KEY =
            "care-relationship.already-established";
    private static final String ALREADY_ENDED_MESSAGE_KEY =
            "care-relationship.already-ended";

    private CareRelationshipId id;
    private UserId userId;
    private CareRecipientProfileId careRecipientProfileId;
    private RelationshipType relationshipType;
    private CareRelationshipStatus status;
    private Instant startedAt;
    private Instant endedAt;

    /** Reconstitution constructor, used by the persistence assembler. */
    public CareRelationship() {
    }

    public CareRelationship(
            EstablishCareRelationshipCommand command,
            Instant startedAt) {

        if (command == null || command.userId() == null) {
            throw new IllegalArgumentException("user.id.invalid");
        }

        if (command.careRecipientProfileId() == null) {
            throw new IllegalArgumentException("care-recipient-profile.id.invalid");
        }

        if (command.relationshipType() == null) {
            throw new IllegalArgumentException(INVALID_RELATIONSHIP_TYPE_MESSAGE_KEY);
        }

        this.id = CareRelationshipId.generate();
        this.userId = new UserId(command.userId());
        this.careRecipientProfileId =
                new CareRecipientProfileId(command.careRecipientProfileId());
        this.relationshipType = command.relationshipType();

        establish(startedAt);
    }

    public void establish(Instant startedAt) {
        validateTimestamp(startedAt);

        if (this.status != null) {
            throw new IllegalStateException(ALREADY_ESTABLISHED_MESSAGE_KEY);
        }

        this.status = CareRelationshipStatus.ACTIVE;
        this.startedAt = startedAt;
        this.endedAt = null;

        registerDomainEvent(CareRelationshipEstablishedEvent.from(this));
    }

    public void end(Instant endedAt) {
        validateTimestamp(endedAt);

        if (!isActive()) {
            throw new IllegalStateException(ALREADY_ENDED_MESSAGE_KEY);
        }

        this.status = CareRelationshipStatus.ENDED;
        this.endedAt = endedAt;

        registerDomainEvent(CareRelationshipEndedEvent.from(this));
    }

    public boolean isActive() {
        return CareRelationshipStatus.ACTIVE.equals(status);
    }

    private static void validateTimestamp(Instant timestamp) {
        if (timestamp == null) {
            throw new IllegalArgumentException(INVALID_TIMESTAMP_MESSAGE_KEY);
        }
    }

    public void setId(CareRelationshipId id) {
        this.id = id;
    }

    public void setUserId(UserId userId) {
        this.userId = userId;
    }

    public void setCareRecipientProfileId(
            CareRecipientProfileId careRecipientProfileId) {
        this.careRecipientProfileId = careRecipientProfileId;
    }

    public void setRelationshipType(RelationshipType relationshipType) {
        this.relationshipType = relationshipType;
    }

    public void setStatus(CareRelationshipStatus status) {
        this.status = status;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public void setEndedAt(Instant endedAt) {
        this.endedAt = endedAt;
    }
}