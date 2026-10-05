package com.healthify.guardian.platform.healthmonitoring.domain.repositories;

import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSign;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.DateRange;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignType;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.WearableDeviceId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Vital sign aggregate repository port.
 */
public interface VitalSignRepository {

    /**
     * Persists a vital sign and publishes its registered domain events.
     */
    VitalSign save(VitalSign vitalSign);

    /**
     * Persists several vital signs and publishes their registered domain events.
     */
    List<VitalSign> saveAll(List<VitalSign> vitalSigns);

    Optional<VitalSign> findById(VitalSignId id);

    /**
     * Retrieves the most recent emitted reading of a type for a care recipient.
     */
    Optional<VitalSign> findLatestByCareRecipientProfileIdAndVitalSignType(
            CareRecipientProfileId careRecipientProfileId, VitalSignType vitalSignType);

    /**
     * Retrieves the most recent readings of a type for a care recipient, newest first.
     *
     * @param count maximum number of readings to return
     */
    List<VitalSign> findRecentByCareRecipientProfileIdAndVitalSignType(
            CareRecipientProfileId careRecipientProfileId, VitalSignType vitalSignType, int count);

    /**
     * Retrieves the readings of a care recipient measured within a period (whole UTC days), oldest first.
     */
    List<VitalSign> findByCareRecipientProfileIdAndPeriod(CareRecipientProfileId careRecipientProfileId, DateRange period);

    /**
     * Whether a reading was already stored for the same device, type and measurement instant,
     * used to drop duplicates re-sent by the offline buffer of the wearable (US21).
     */
    boolean existsByWearableDeviceIdAndVitalSignTypeAndMeasuredAt(
            WearableDeviceId wearableDeviceId, VitalSignType vitalSignType, Instant measuredAt);
}
