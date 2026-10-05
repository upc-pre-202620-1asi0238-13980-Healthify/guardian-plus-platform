package com.healthify.guardian.platform.healthmonitoring.domain.model.events;

import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignTypeId;

import java.time.Instant;

/**
 * Raised after a vital sign reading has been validated and captured.
 */
public record VitalSignsDetectedEvent(
        VitalSignId vitalSignId,
        CareRecipientProfileId careRecipientProfileId,
        VitalSignTypeId vitalSignTypeId,
        Instant measuredAt) {
}
