package com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates;

import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.ConfigureAlertChannelCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.events.AlertChannelSettingChangedEvent;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertChannelSettingId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.NotificationChannel;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.UserId;
import com.healthify.guardian.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

/**
 * Aggregate root recording whether a notification channel is enabled for a Care Circle member.
 * There is one per combination of {@code UserId} and {@code NotificationChannel}.
 *
 * <p>For the {@code PUSH} channel it also holds the device token the push provider delivers to.</p>
 */
@Getter
public class AlertChannelSetting extends AbstractDomainAggregateRoot<AlertChannelSetting> {

    private static final String CHANNEL_INVALID_MESSAGE_KEY = "alert-channel-setting.channel.invalid";
    private static final String ENABLED_INVALID_MESSAGE_KEY = "alert-channel-setting.enabled.invalid";
    private static final String DEVICE_TOKEN_NOT_APPLICABLE_MESSAGE_KEY = "alert-channel-setting.device-token.not-applicable";
    private static final String DEVICE_TOKEN_INVALID_MESSAGE_KEY = "alert-channel-setting.device-token.invalid";

    private AlertChannelSettingId id;
    private UserId userId;
    private NotificationChannel channel;
    private boolean enabled;
    private String deviceToken;

    /** Reconstitution constructor, used by the persistence assembler. */
    public AlertChannelSetting() {
    }

    /** Configures a channel for a Care Circle member for the first time. */
    public AlertChannelSetting(ConfigureAlertChannelCommand command) {
        if (command.channel() == null) {
            throw new IllegalArgumentException(CHANNEL_INVALID_MESSAGE_KEY);
        }
        if (command.enabled() == null) {
            throw new IllegalArgumentException(ENABLED_INVALID_MESSAGE_KEY);
        }
        this.id = AlertChannelSettingId.generate();
        this.userId = new UserId(command.userId());
        this.channel = command.channel();
        this.enabled = command.enabled();
        if (command.deviceToken() != null) {
            registerDeviceToken(command.deviceToken());
        }
        registerDomainEvent(AlertChannelSettingChangedEvent.from(this));
    }

    public void enable() {
        if (enabled) {
            return;
        }
        this.enabled = true;
        registerDomainEvent(AlertChannelSettingChangedEvent.from(this));
    }

    public void disable() {
        if (!enabled) {
            return;
        }
        this.enabled = false;
        registerDomainEvent(AlertChannelSettingChangedEvent.from(this));
    }

    /** Records the device token the push provider must deliver to. Only meaningful for {@code PUSH}. */
    public void registerDeviceToken(String deviceToken) {
        if (channel != NotificationChannel.PUSH) {
            throw new IllegalArgumentException(DEVICE_TOKEN_NOT_APPLICABLE_MESSAGE_KEY);
        }
        if (deviceToken == null || deviceToken.isBlank()) {
            throw new IllegalArgumentException(DEVICE_TOKEN_INVALID_MESSAGE_KEY);
        }
        this.deviceToken = deviceToken.strip();
    }

    /** Restores an identity and state from persistence. Used by the persistence assembler. */
    public void setId(AlertChannelSettingId id) {
        this.id = id;
    }

    public void setUserId(UserId userId) {
        this.userId = userId;
    }

    public void setChannel(NotificationChannel channel) {
        this.channel = channel;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }
}
