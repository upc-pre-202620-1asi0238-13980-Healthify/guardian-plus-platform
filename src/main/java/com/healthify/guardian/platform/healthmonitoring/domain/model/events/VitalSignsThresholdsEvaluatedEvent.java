package com.healthify.guardian.platform.healthmonitoring.domain.model.events;

import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.ReadingClassification;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignThresholdId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignTypeId;

import java.time.Instant;

/**
 * Raised after a reading has been evaluated against the threshold in force, carrying whether it deviated.
 */
public record VitalSignsThresholdsEvaluatedEvent(
        VitalSignId vitalSignId,
        CareRecipientProfileId careRecipientProfileId,
        VitalSignTypeId vitalSignTypeId,
        VitalSignThresholdId vitalSignThresholdId,
        ReadingClassification classification,
        boolean hasDeviation,
        Instant evaluatedAt) {
}
