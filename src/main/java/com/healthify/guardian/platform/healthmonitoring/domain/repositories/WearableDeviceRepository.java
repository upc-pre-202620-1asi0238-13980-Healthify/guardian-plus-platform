package com.healthify.guardian.platform.healthmonitoring.domain.repositories;

import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.WearableDevice;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.SerialNumber;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.WearableDeviceId;

import java.util.List;
import java.util.Optional;

/**
 * Wearable device aggregate repository port.
 */
public interface WearableDeviceRepository {

    WearableDevice save(WearableDevice device);

    Optional<WearableDevice> findById(WearableDeviceId id);

    List<WearableDevice> findByCareRecipientProfileId(CareRecipientProfileId careRecipientProfileId);

    Optional<WearableDevice> findBySerialNumber(SerialNumber serialNumber);

    /**
     * Retrieves every linked device, used to know which care recipients are monitored.
     */
    List<WearableDevice> findAll();
}
