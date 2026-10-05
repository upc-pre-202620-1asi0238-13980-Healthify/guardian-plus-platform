package com.healthify.guardian.platform.healthmonitoring.domain.repositories;

import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSignThreshold;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignThresholdId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignTypeId;

import java.util.List;
import java.util.Optional;

/**
 * Vital sign threshold aggregate repository port. At most one threshold exists per care
 * recipient and vital sign type.
 */
public interface VitalSignThresholdRepository {

    VitalSignThreshold save(VitalSignThreshold threshold);

    Optional<VitalSignThreshold> findById(VitalSignThresholdId id);

    Optional<VitalSignThreshold> findByCareRecipientProfileIdAndVitalSignTypeId(
            CareRecipientProfileId careRecipientProfileId, VitalSignTypeId vitalSignTypeId);

    List<VitalSignThreshold> findAllActiveByCareRecipientProfileId(CareRecipientProfileId careRecipientProfileId);
}
