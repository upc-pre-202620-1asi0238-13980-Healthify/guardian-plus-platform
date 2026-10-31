package com.healthify.guardian.platform.emergencyalerting.domain.model.commands;

import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.DeliveryStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Command to record the outcome a notification provider reported for an alert delivery.
 *
 * @param alertId        the alert the delivery belongs to
 * @param deliveryId     the delivery the outcome refers to
 * @param deliveryStatus the reported outcome
 * @param occurredAt     when the provider observed the outcome
 */
public record RegisterDeliveryResultCommand(UUID alertId, UUID deliveryId, DeliveryStatus deliveryStatus, Instant occurredAt) {
}
