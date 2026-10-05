package com.healthify.guardian.platform.healthmonitoring.domain.model.events;

import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.ReadingClassification;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignType;

import java.time.Instant;

/**
 * Raised after a reading has been evaluated against the normal range of its vital sign type,
 * carrying whether it deviated.
 */
public record VitalSignsThresholdsEvaluatedEvent(
        VitalSignId vitalSignId,
        CareRecipientProfileId careRecipientProfileId,
        VitalSignType vitalSignType,
        ReadingClassification classification,
        boolean hasDeviation,
        Instant evaluatedAt) {
}
