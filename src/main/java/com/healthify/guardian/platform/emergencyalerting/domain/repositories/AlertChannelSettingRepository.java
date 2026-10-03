package com.healthify.guardian.platform.emergencyalerting.domain.repositories;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.AlertChannelSetting;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.NotificationChannel;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.UserId;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * AlertChannelSetting aggregate repository port.
 */
public interface AlertChannelSettingRepository {

    /**
     * Persists a channel setting and publishes its registered domain events.
     *
     * @param setting the setting to save
     * @return the saved setting
     */
    AlertChannelSetting save(AlertChannelSetting setting);

    List<AlertChannelSetting> findByUserId(UserId userId);

    /**
     * Retrieves the channel settings of several users at once, used to resolve the channels of
     * every recipient of an escalation level in a single lookup.
     *
     * @param userIds the users whose channel settings are requested
     * @return their channel settings
     */
    List<AlertChannelSetting> findByUserIdIn(Collection<UserId> userIds);

    Optional<AlertChannelSetting> findByUserIdAndChannel(UserId userId, NotificationChannel channel);
}
