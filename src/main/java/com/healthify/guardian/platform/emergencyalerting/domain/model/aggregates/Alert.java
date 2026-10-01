package com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates;

import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.TriggerAlertCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.entities.AlertDelivery;
import com.healthify.guardian.platform.emergencyalerting.domain.model.entities.AlertResponse;
import com.healthify.guardian.platform.emergencyalerting.domain.model.events.AlertAcknowledgedEvent;
import com.healthify.guardian.platform.emergencyalerting.domain.model.events.AlertBroadcastedEvent;
import com.healthify.guardian.platform.emergencyalerting.domain.model.events.AlertConfirmedEvent;
import com.healthify.guardian.platform.emergencyalerting.domain.model.events.AlertDeliveryFailedEvent;
import com.healthify.guardian.platform.emergencyalerting.domain.model.events.AlertDismissedEvent;
import com.healthify.guardian.platform.emergencyalerting.domain.model.events.AlertDispatchedEvent;
import com.healthify.guardian.platform.emergencyalerting.domain.model.events.AlertEscalatedEvent;
import com.healthify.guardian.platform.emergencyalerting.domain.model.events.AlertResolvedEvent;
import com.healthify.guardian.platform.emergencyalerting.domain.model.events.AlertResponseClaimedEvent;
import com.healthify.guardian.platform.emergencyalerting.domain.model.events.AlertResponseCompletedEvent;
import com.healthify.guardian.platform.emergencyalerting.domain.model.events.AlertTriggeredEvent;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AckTimeout;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertDeliveryId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertResponseId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertSource;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertStatus;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.DeliveryStatus;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.DeliveryTarget;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.FallConfirmationWindow;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.RecipientLevel;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.Severity;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.UserId;
import com.healthify.guardian.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Aggregate root representing the alert raised by a risk signal on a Fragile Citizen, together
 * with its deliveries to the emergency contacts and the Care Circle's responses.
 *
 * <p>Governs its own lifecycle ({@code PENDING_CONFIRMATION → TRIGGERED → ESCALATED →
 * ACKNOWLEDGED → RESOLVED}, with {@code DISMISSED} for a false positive) and rejects invalid
 * transitions on its own. Deciding <em>which</em> level and recipients to dispatch to is left to
 * {@code DispatchStrategyPolicy} and {@code EscalationPolicy}; this aggregate only records
 * whatever targets those policies hand back.</p>
 *
 * <p>Every time-dependent operation receives the current instant instead of reading the clock,
 * so callers decide the time source and the timing rules can be tested deterministically.</p>
 */
@Getter
public class Alert extends AbstractDomainAggregateRoot<Alert> {

    private static final String TRIGGERED_AT_INVALID_MESSAGE_KEY = "alert.triggered-at.invalid";
    private static final String INVALID_TRANSITION_MESSAGE_KEY = "alert.invalid-transition";
    private static final String CANNOT_CONFIRM_MESSAGE_KEY = "alert.cannot.confirm";
    private static final String CANNOT_DISMISS_MESSAGE_KEY = "alert.cannot.dismiss";
    private static final String WINDOW_EXPIRED_MESSAGE_KEY = "alert.confirmation-window.expired";
    private static final String CANNOT_DISPATCH_MESSAGE_KEY = "alert.cannot.dispatch";
    private static final String ALREADY_DISPATCHED_MESSAGE_KEY = "alert.already-dispatched";
    private static final String NO_TARGETS_MESSAGE_KEY = "alert.dispatch.no-targets";
    private static final String CANNOT_ESCALATE_MESSAGE_KEY = "alert.cannot.escalate";
    private static final String CANNOT_BROADCAST_MESSAGE_KEY = "alert.cannot.broadcast";
    private static final String CANNOT_ACKNOWLEDGE_MESSAGE_KEY = "alert.cannot.acknowledge";
    private static final String NOT_A_RECIPIENT_MESSAGE_KEY = "alert.not-a-recipient";
    private static final String CANNOT_CLAIM_MESSAGE_KEY = "alert.cannot.claim-response";
    private static final String ALREADY_CLAIMED_MESSAGE_KEY = "alert.response.already-claimed";
    private static final String CANNOT_RESOLVE_MESSAGE_KEY = "alert.cannot.resolve";
    private static final String DELIVERY_NOT_FOUND_MESSAGE_KEY = "alert.delivery.not-found";
    private static final String DELIVERY_RESULT_INVALID_MESSAGE_KEY = "alert.delivery.result.invalid";
    private static final String RESPONSE_NOT_FOUND_MESSAGE_KEY = "alert.response.not-found";

    private AlertId id;
    private CareRecipientProfileId careRecipientProfileId;
    private AlertSource source;
    private Severity severity;
    private AlertStatus status;
    private List<AlertDelivery> deliveries = new ArrayList<>();
    private List<AlertResponse> responses = new ArrayList<>();
    private Instant triggeredAt;
    private Instant confirmedAt;
    private Instant lastDispatchedAt;
    private Instant acknowledgedAt;
    private UserId acknowledgedByUserId;
    private Instant resolvedAt;

    /** Reconstitution constructor, used by the persistence assembler. */
    public Alert() {
    }

    /**
     * Triggers a new alert. It starts in {@code PENDING_CONFIRMATION} when its source grants the
     * Fragile Citizen a window to cancel it (a detected fall), or in {@code TRIGGERED} otherwise.
     * The severity is derived from the source type.
     */
    public Alert(TriggerAlertCommand command) {
        this.source = new AlertSource(command.sourceType(), command.sourceReferenceId());
        if (command.triggeredAt() == null) {
            throw new IllegalArgumentException(TRIGGERED_AT_INVALID_MESSAGE_KEY);
        }
        this.id = AlertId.generate();
        this.careRecipientProfileId = new CareRecipientProfileId(command.careRecipientProfileId());
        this.severity = source.sourceType().defaultSeverity();
        this.triggeredAt = command.triggeredAt();
        this.status = source.requiresConfirmationWindow() ? AlertStatus.PENDING_CONFIRMATION : AlertStatus.TRIGGERED;
        registerDomainEvent(AlertTriggeredEvent.from(this));
    }

    /**
     * Confirms this alert so it can be dispatched: once its fall confirmation window has expired,
     * or right away when its source does not require one.
     */
    public void confirm(Instant confirmedAt) {
        if (isConfirmed()) {
            throw new IllegalStateException(CANNOT_CONFIRM_MESSAGE_KEY);
        }
        if (status == AlertStatus.PENDING_CONFIRMATION) {
            transitionTo(AlertStatus.TRIGGERED);
        } else if (status != AlertStatus.TRIGGERED) {
            throw new IllegalStateException(CANNOT_CONFIRM_MESSAGE_KEY);
        }
        this.confirmedAt = confirmedAt;
        registerDomainEvent(AlertConfirmedEvent.from(this));
    }

    /** Classifies this alert as a false positive because the Fragile Citizen cancelled it in time. */
    public void dismissAsFalsePositive(Instant now) {
        if (status != AlertStatus.PENDING_CONFIRMATION) {
            throw new IllegalStateException(CANNOT_DISMISS_MESSAGE_KEY);
        }
        if (FallConfirmationWindow.hasExpired(triggeredAt, now)) {
            throw new IllegalStateException(WINDOW_EXPIRED_MESSAGE_KEY);
        }
        transitionTo(AlertStatus.DISMISSED);
        registerDomainEvent(AlertDismissedEvent.from(this, now));
    }

    /**
     * Generates the deliveries of this alert's initial recipient level.
     *
     * @param level   the level decided by {@code DispatchStrategyPolicy}
     * @param targets the recipients and channels resolved by {@code EscalationPolicy}
     * @param now     the dispatch time
     * @return the deliveries just created, still pending to be sent
     */
    public List<AlertDelivery> dispatch(RecipientLevel level, List<DeliveryTarget> targets, Instant now) {
        if (!isConfirmed() || !status.isAwaitingAcknowledgement()) {
            throw new IllegalStateException(CANNOT_DISPATCH_MESSAGE_KEY);
        }
        if (!deliveries.isEmpty()) {
            throw new IllegalStateException(ALREADY_DISPATCHED_MESSAGE_KEY);
        }
        var created = addDeliveries(level, targets, now);
        registerDomainEvent(AlertDispatchedEvent.from(this, level, created, true));
        if (level == RecipientLevel.BROADCAST) {
            registerDomainEvent(AlertBroadcastedEvent.from(this));
        }
        return created;
    }

    /**
     * Escalates this still unacknowledged alert from its primary contact to its secondary contacts.
     *
     * @param targets the secondary recipients and channels resolved by {@code EscalationPolicy}
     * @param now     the escalation time
     * @return the deliveries just created
     */
    public List<AlertDelivery> escalate(List<DeliveryTarget> targets, Instant now) {
        if (!isAwaitingAcknowledgement()
                || currentRecipientLevel() != RecipientLevel.PRIMARY
                || !severity.allowsEscalation()) {
            throw new IllegalStateException(CANNOT_ESCALATE_MESSAGE_KEY);
        }
        var created = addDeliveries(RecipientLevel.SECONDARY, targets, now);
        transitionTo(AlertStatus.ESCALATED);
        registerDomainEvent(AlertEscalatedEvent.from(this));
        registerDomainEvent(AlertDispatchedEvent.from(this, RecipientLevel.SECONDARY, created, false));
        return created;
    }

    /**
     * Broadcasts this still unacknowledged alert to every active contact as the last resort of the
     * escalation chain (<em>Critical Broadcast Fallback</em>).
     *
     * @param targets every active recipient and channel resolved by {@code EscalationPolicy}
     * @param now     the broadcast time
     * @return the deliveries just created
     */
    public List<AlertDelivery> broadcast(List<DeliveryTarget> targets, Instant now) {
        var current = currentRecipientLevel();
        if (!isAwaitingAcknowledgement()
                || current == RecipientLevel.BROADCAST
                || !severity.allowsEscalation()) {
            throw new IllegalStateException(CANNOT_BROADCAST_MESSAGE_KEY);
        }
        var created = addDeliveries(RecipientLevel.BROADCAST, targets, now);
        if (status != AlertStatus.ESCALATED) {
            transitionTo(AlertStatus.ESCALATED);
        }
        registerDomainEvent(AlertBroadcastedEvent.from(this));
        registerDomainEvent(AlertDispatchedEvent.from(this, RecipientLevel.BROADCAST, created, false));
        return created;
    }

    /**
     * Records the outcome the notification provider reported for one of this alert's deliveries.
     *
     * <p>Providers may repeat or reorder callbacks, so a result identical to the current one, or any
     * result arriving after the delivery reached a final status, is ignored.</p>
     */
    public void registerDeliveryResult(AlertDeliveryId deliveryId, DeliveryStatus result, Instant occurredAt) {
        var delivery = findDelivery(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException(DELIVERY_NOT_FOUND_MESSAGE_KEY));
        if (result == null || result == DeliveryStatus.PENDING) {
            throw new IllegalArgumentException(DELIVERY_RESULT_INVALID_MESSAGE_KEY);
        }
        if (delivery.getDeliveryStatus() == result || delivery.getDeliveryStatus().isFinal()) {
            return;
        }
        switch (result) {
            case SENT -> delivery.markAsSent(occurredAt);
            case DELIVERED -> delivery.markAsDelivered(occurredAt);
            case FAILED -> {
                delivery.markAsFailed();
                registerDomainEvent(AlertDeliveryFailedEvent.from(this, delivery, occurredAt));
            }
            default -> throw new IllegalArgumentException(DELIVERY_RESULT_INVALID_MESSAGE_KEY);
        }
    }

    /**
     * Records that one of this alert's recipients acknowledged it, which stops its escalation
     * (<em>Escalation Stopper</em>).
     */
    public void acknowledge(UserId userId, Instant now) {
        if (!isAwaitingAcknowledgement()) {
            throw new IllegalStateException(CANNOT_ACKNOWLEDGE_MESSAGE_KEY);
        }
        if (!wasNotified(userId)) {
            throw new IllegalStateException(NOT_A_RECIPIENT_MESSAGE_KEY);
        }
        transitionTo(AlertStatus.ACKNOWLEDGED);
        this.acknowledgedAt = now;
        this.acknowledgedByUserId = userId;
        registerDomainEvent(AlertAcknowledgedEvent.from(this));
    }

    /**
     * Records that one of this alert's recipients takes charge of responding to it. Only one
     * response may be in progress at a time. A recipient on their way to help also acknowledges the
     * alert if nobody had done so yet, so its escalation stops.
     *
     * @return the response just claimed
     */
    public AlertResponse claimResponse(UserId responderUserId, Instant now) {
        if (!isAwaitingAcknowledgement() && status != AlertStatus.ACKNOWLEDGED) {
            throw new IllegalStateException(CANNOT_CLAIM_MESSAGE_KEY);
        }
        if (!wasNotified(responderUserId)) {
            throw new IllegalStateException(NOT_A_RECIPIENT_MESSAGE_KEY);
        }
        if (responses.stream().anyMatch(AlertResponse::isActive)) {
            throw new IllegalStateException(ALREADY_CLAIMED_MESSAGE_KEY);
        }
        if (status != AlertStatus.ACKNOWLEDGED) {
            acknowledge(responderUserId, now);
        }
        var response = new AlertResponse(responderUserId, now);
        responses.add(response);
        registerDomainEvent(AlertResponseClaimedEvent.from(this, response));
        return response;
    }

    /** Records the outcome of an intervention previously claimed on this alert. */
    public void completeResponse(AlertResponseId responseId, String notes, Instant now) {
        var response = responses.stream()
                .filter(candidate -> candidate.getId().equals(responseId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(RESPONSE_NOT_FOUND_MESSAGE_KEY));
        response.complete(notes, now);
        registerDomainEvent(AlertResponseCompletedEvent.from(this, response));
    }

    /** Definitively closes this alert once its incident is closed, withdrawing any response still in progress. */
    public void resolve(Instant now) {
        if (!status.canTransitionTo(AlertStatus.RESOLVED)) {
            throw new IllegalStateException(CANNOT_RESOLVE_MESSAGE_KEY);
        }
        transitionTo(AlertStatus.RESOLVED);
        this.resolvedAt = now;
        responses.stream().filter(AlertResponse::isActive).forEach(AlertResponse::cancel);
        registerDomainEvent(AlertResolvedEvent.from(this));
    }

    /** Returns the furthest escalation level this alert has been dispatched at, or {@code null} if not dispatched yet. */
    public RecipientLevel currentRecipientLevel() {
        return deliveries.stream()
                .map(AlertDelivery::getRecipientLevel)
                .max(Comparator.naturalOrder())
                .orElse(null);
    }

    /** True once this alert has been confirmed and may be dispatched. */
    public boolean isConfirmed() {
        return confirmedAt != null;
    }

    /** True while this alert has been dispatched and nobody has acknowledged it yet. */
    public boolean isAwaitingAcknowledgement() {
        return status.isAwaitingAcknowledgement() && !deliveries.isEmpty();
    }

    /** True if this alert still awaits acknowledgement and its last dispatch is older than the given timeout. */
    public boolean isAckTimeoutExpired(AckTimeout ackTimeout, Instant now) {
        return isAwaitingAcknowledgement() && ackTimeout.hasExpired(lastDispatchedAt, now);
    }

    /** True if this alert is still pending confirmation and its fall confirmation window has elapsed. */
    public boolean isConfirmationWindowExpired(Instant now) {
        return status == AlertStatus.PENDING_CONFIRMATION && FallConfirmationWindow.hasExpired(triggeredAt, now);
    }

    /** True while this alert has not been dismissed or resolved. */
    public boolean isActive() {
        return !status.isTerminal();
    }

    /** True if the given user received at least one delivery of this alert. */
    public boolean wasNotified(UserId userId) {
        return deliveries.stream().anyMatch(delivery -> delivery.getRecipientUserId().equals(userId));
    }

    public Optional<AlertDelivery> findDelivery(AlertDeliveryId deliveryId) {
        return deliveries.stream().filter(delivery -> delivery.getId().equals(deliveryId)).findFirst();
    }

    public List<AlertDelivery> getDeliveries() {
        return Collections.unmodifiableList(deliveries);
    }

    public List<AlertResponse> getResponses() {
        return Collections.unmodifiableList(responses);
    }

    private List<AlertDelivery> addDeliveries(RecipientLevel level, List<DeliveryTarget> targets, Instant now) {
        if (targets == null || targets.isEmpty()) {
            throw new IllegalStateException(NO_TARGETS_MESSAGE_KEY);
        }
        var created = targets.stream().map(target -> new AlertDelivery(target, level)).toList();
        deliveries.addAll(created);
        this.lastDispatchedAt = now;
        return created;
    }

    private void transitionTo(AlertStatus target) {
        if (!status.canTransitionTo(target)) {
            throw new IllegalStateException(INVALID_TRANSITION_MESSAGE_KEY);
        }
        this.status = target;
    }

    /** Restores an identity and state from persistence. Used by the persistence assembler. */
    public void setId(AlertId id) {
        this.id = id;
    }

    public void setCareRecipientProfileId(CareRecipientProfileId careRecipientProfileId) {
        this.careRecipientProfileId = careRecipientProfileId;
    }

    public void setSource(AlertSource source) {
        this.source = source;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public void setStatus(AlertStatus status) {
        this.status = status;
    }

    public void setDeliveries(List<AlertDelivery> deliveries) {
        this.deliveries = new ArrayList<>(deliveries);
    }

    public void setResponses(List<AlertResponse> responses) {
        this.responses = new ArrayList<>(responses);
    }

    public void setTriggeredAt(Instant triggeredAt) {
        this.triggeredAt = triggeredAt;
    }

    public void setConfirmedAt(Instant confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public void setLastDispatchedAt(Instant lastDispatchedAt) {
        this.lastDispatchedAt = lastDispatchedAt;
    }

    public void setAcknowledgedAt(Instant acknowledgedAt) {
        this.acknowledgedAt = acknowledgedAt;
    }

    public void setAcknowledgedByUserId(UserId acknowledgedByUserId) {
        this.acknowledgedByUserId = acknowledgedByUserId;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}
