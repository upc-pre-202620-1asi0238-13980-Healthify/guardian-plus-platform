package com.healthify.guardian.platform.emergencyalerting.interfaces.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Integration event published by {@code emergencyAlerting} when an incident is definitively
 * closed, so it can be added to the Fragile Citizen's health history.
 *
 * @param incidentId             the closed incident
 * @param alertId                the alert the incident attended
 * @param careRecipientProfileId the Fragile Citizen the incident is about
 * @param sourceType             the kind of signal that raised the alert
 * @param notes                  the notes recorded while attending the incident
 * @param closedAt               when the incident was closed
 */
public record IncidentClosedIntegrationEvent(
        UUID incidentId,
        UUID alertId,
        UUID careRecipientProfileId,
        String sourceType,
        String notes,
        Instant closedAt) {
}
