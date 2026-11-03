package com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources;

import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.DeliveryStatus;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

/**
 * Payload a notification provider posts to report the outcome of a delivery.
 *
 * @param alertId    the alert the delivery belongs to
 * @param deliveryId the delivery the outcome refers to
 * @param status     {@code SENT}, {@code DELIVERED} or {@code FAILED}
 * @param occurredAt when the provider observed the outcome; the reception time if omitted
 */
public record DeliveryStatusCallbackResource(
        @NotNull(message = "{alert.id.invalid}")
        UUID alertId,

        @NotNull(message = "{alert-delivery.id.invalid}")
        UUID deliveryId,

        @NotNull(message = "{alert.delivery.result.invalid}")
        DeliveryStatus status,

        Instant occurredAt
) {
}
