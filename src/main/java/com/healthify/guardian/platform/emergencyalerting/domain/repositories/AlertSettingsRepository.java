package com.healthify.guardian.platform.emergencyalerting.domain.repositories;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.AlertSettings;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;

import java.util.Optional;

/**
 * AlertSettings aggregate repository port.
 */
public interface AlertSettingsRepository {

    /**
     * Persists a Fragile Citizen's alerting configuration and publishes its registered domain events.
     *
     * @param settings the settings to save
     * @return the saved settings
     */
    AlertSettings save(AlertSettings settings);

    Optional<AlertSettings> findByCareRecipientProfileId(CareRecipientProfileId careRecipientProfileId);
}
