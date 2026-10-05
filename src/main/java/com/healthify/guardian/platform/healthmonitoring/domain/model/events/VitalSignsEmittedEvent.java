package com.healthify.guardian.platform.healthmonitoring.domain.model.events;

import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignType;

import java.time.Instant;

/**
 * Raised when a detected reading is published for live consumption.
 */
public record VitalSignsEmittedEvent(
        VitalSignId vitalSignId,
        CareRecipientProfileId careRecipientProfileId,
        VitalSignType vitalSignType,
        Instant emittedAt) {
}
