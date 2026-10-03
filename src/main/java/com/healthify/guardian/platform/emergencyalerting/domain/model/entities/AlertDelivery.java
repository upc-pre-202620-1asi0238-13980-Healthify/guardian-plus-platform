package com.healthify.guardian.platform.emergencyalerting.domain.model.entities;

import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertDeliveryId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.DeliveryStatus;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.DeliveryTarget;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.NotificationChannel;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.RecipientLevel;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.UserId;
import lombok.Getter;

import java.time.Instant;

/**
 * Entity internal to the {@code Alert} aggregate: the delivery of an alert to one recipient,
 * through one channel, at one level of the escalation chain.
 */
@Getter
public class AlertDelivery {

    private static final String CANNOT_MARK_SENT_MESSAGE_KEY = "alert-delivery.cannot.mark-sent";
    private static final String CANNOT_MARK_DELIVERED_MESSAGE_KEY = "alert-delivery.cannot.mark-delivered";
    private static final String CANNOT_MARK_FAILED_MESSAGE_KEY = "alert-delivery.cannot.mark-failed";

    private final AlertDeliveryId id;
    private final UserId recipientUserId;
    private final RecipientLevel recipientLevel;
    private final NotificationChannel channel;
    private DeliveryStatus deliveryStatus;
    private Instant sentAt;
    private Instant deliveredAt;

    /** Creates a new, still pending delivery for the given target. */
    public AlertDelivery(DeliveryTarget target, RecipientLevel recipientLevel) {
        this(AlertDeliveryId.generate(), target.recipientUserId(), recipientLevel, target.channel(),
                DeliveryStatus.PENDING, null, null);
    }

    /** Reconstitution constructor, used by the persistence assembler. */
    public AlertDelivery(
            AlertDeliveryId id,
            UserId recipientUserId,
            RecipientLevel recipientLevel,
            NotificationChannel channel,
            DeliveryStatus deliveryStatus,
            Instant sentAt,
            Instant deliveredAt) {
        this.id = id;
        this.recipientUserId = recipientUserId;
        this.recipientLevel = recipientLevel;
        this.channel = channel;
        this.deliveryStatus = deliveryStatus;
        this.sentAt = sentAt;
        this.deliveredAt = deliveredAt;
    }

    /** Records that the provider accepted the delivery. */
    public void markAsSent(Instant sentAt) {
        if (deliveryStatus != DeliveryStatus.PENDING) {
            throw new IllegalStateException(CANNOT_MARK_SENT_MESSAGE_KEY);
        }
        this.deliveryStatus = DeliveryStatus.SENT;
        this.sentAt = sentAt;
    }

    /** Records that the provider confirmed the delivery reached the recipient's device. */
    public void markAsDelivered(Instant deliveredAt) {
        if (deliveryStatus.isFinal()) {
            throw new IllegalStateException(CANNOT_MARK_DELIVERED_MESSAGE_KEY);
        }
        if (this.sentAt == null) {
            this.sentAt = deliveredAt;
        }
        this.deliveryStatus = DeliveryStatus.DELIVERED;
        this.deliveredAt = deliveredAt;
    }

    /** Records that the provider could not complete the delivery. */
    public void markAsFailed() {
        if (deliveryStatus.isFinal()) {
            throw new IllegalStateException(CANNOT_MARK_FAILED_MESSAGE_KEY);
        }
        this.deliveryStatus = DeliveryStatus.FAILED;
    }
}
