package com.healthify.guardian.platform.emergencyalerting.domain.model.entities;

import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertResponseId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.ResponseStatus;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.UserId;
import lombok.Getter;

import java.time.Instant;

/**
 * Entity internal to the {@code Alert} aggregate: a Care Circle member taking charge of
 * responding to the alert, and the outcome of their intervention.
 */
@Getter
public class AlertResponse {

    private static final String CANNOT_COMPLETE_MESSAGE_KEY = "alert-response.cannot.complete";
    private static final String CANNOT_CANCEL_MESSAGE_KEY = "alert-response.cannot.cancel";

    private final AlertResponseId id;
    private final UserId responderUserId;
    private ResponseStatus responseStatus;
    private final Instant claimedAt;
    private Instant completedAt;
    private String notes;

    /** Creates a new response claimed by the given Care Circle member. */
    public AlertResponse(UserId responderUserId, Instant claimedAt) {
        this(AlertResponseId.generate(), responderUserId, ResponseStatus.CLAIMED, claimedAt, null, null);
    }

    /** Reconstitution constructor, used by the persistence assembler. */
    public AlertResponse(
            AlertResponseId id,
            UserId responderUserId,
            ResponseStatus responseStatus,
            Instant claimedAt,
            Instant completedAt,
            String notes) {
        this.id = id;
        this.responderUserId = responderUserId;
        this.responseStatus = responseStatus;
        this.claimedAt = claimedAt;
        this.completedAt = completedAt;
        this.notes = notes;
    }

    /** Records the outcome of the intervention. */
    public void complete(String notes, Instant completedAt) {
        if (responseStatus != ResponseStatus.CLAIMED) {
            throw new IllegalStateException(CANNOT_COMPLETE_MESSAGE_KEY);
        }
        this.responseStatus = ResponseStatus.COMPLETED;
        this.completedAt = completedAt;
        this.notes = notes;
    }

    /** Withdraws a claim that was never completed. */
    public void cancel() {
        if (responseStatus != ResponseStatus.CLAIMED) {
            throw new IllegalStateException(CANNOT_CANCEL_MESSAGE_KEY);
        }
        this.responseStatus = ResponseStatus.CANCELLED;
    }

    /** True while the responder is still on their way and no other member may claim the alert. */
    public boolean isActive() {
        return responseStatus == ResponseStatus.CLAIMED;
    }
}
