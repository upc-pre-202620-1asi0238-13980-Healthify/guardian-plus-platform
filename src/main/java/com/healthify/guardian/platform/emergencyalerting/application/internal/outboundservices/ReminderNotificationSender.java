package com.healthify.guardian.platform.emergencyalerting.application.internal.outboundservices;

import com.healthify.guardian.platform.emergencyalerting.application.acl.ProfileContextAcl;
import com.healthify.guardian.platform.emergencyalerting.application.outboundservices.ReminderNotification;
import com.healthify.guardian.platform.emergencyalerting.application.outboundservices.ReminderNotificationDispatcher;
import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.AlertChannelSetting;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.NotificationChannel;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.UserId;
import com.healthify.guardian.platform.emergencyalerting.domain.repositories.AlertChannelSettingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Sends the preventive push notification of a medical appointment to every caregiver of the Fragile Citizen,
 * i.e. every Care Circle member holding an active care relationship with them.
 *
 * <p>Only the {@code PUSH} channel is used, and only for caregivers that registered a device token and did not
 * disable push notifications. These notices are informational, so they are not recorded as alert deliveries.
 * Runs on the notification executor, off the thread that issued the reminder.</p>
 */
@Slf4j
@Service
public class ReminderNotificationSender {

    private final ProfileContextAcl profileContextAcl;
    private final AlertChannelSettingRepository alertChannelSettingRepository;
    private final ReminderNotificationDispatcher reminderNotificationDispatcher;

    public ReminderNotificationSender(
            ProfileContextAcl profileContextAcl,
            AlertChannelSettingRepository alertChannelSettingRepository,
            ReminderNotificationDispatcher reminderNotificationDispatcher) {
        this.profileContextAcl = profileContextAcl;
        this.alertChannelSettingRepository = alertChannelSettingRepository;
        this.reminderNotificationDispatcher = reminderNotificationDispatcher;
    }

    /**
     * Notifies the caregivers of a Fragile Citizen that one of their medical appointments is coming up.
     *
     * @param reminderId             the issued appointment reminder
     * @param careRecipientProfileId the Fragile Citizen
     * @param title                  what the appointment is about
     * @param location               where it takes place, if known
     * @param scheduledTime          when it takes place
     */
    @Async("emergencyAlertingNotificationExecutor")
    public void sendAppointmentReminder(
            UUID reminderId, CareRecipientProfileId careRecipientProfileId, String title, String location,
            Instant scheduledTime) {
        try {
            var caregivers = profileContextAcl.findActiveCaregivers(careRecipientProfileId);
            if (caregivers.isEmpty()) {
                log.info("Appointment reminder {} not pushed: {} has no active caregivers",
                        reminderId, careRecipientProfileId.value());
                return;
            }
            var deviceTokens = pushDeviceTokensOf(caregivers);
            var careRecipientName = profileContextAcl.findCareRecipientDisplayName(careRecipientProfileId).orElse(null);
            for (var caregiver : caregivers) {
                var deviceToken = deviceTokens.get(caregiver);
                if (deviceToken == null) {
                    log.info("Appointment reminder {} not pushed to {}: no enabled PUSH device token",
                            reminderId, caregiver.value());
                    continue;
                }
                send(new ReminderNotification(reminderId, careRecipientProfileId, careRecipientName, title, location,
                        scheduledTime, caregiver, deviceToken));
            }
        } catch (RuntimeException e) {
            log.error("Unexpected failure pushing appointment reminder {}", reminderId, e);
        }
    }

    private Map<UserId, String> pushDeviceTokensOf(List<UserId> caregivers) {
        return alertChannelSettingRepository.findByUserIdIn(caregivers).stream()
                .filter(setting -> setting.getChannel() == NotificationChannel.PUSH)
                .filter(AlertChannelSetting::isEnabled)
                .filter(setting -> setting.getDeviceToken() != null && !setting.getDeviceToken().isBlank())
                .collect(Collectors.toMap(AlertChannelSetting::getUserId, AlertChannelSetting::getDeviceToken,
                        (first, second) -> first));
    }

    private void send(ReminderNotification notification) {
        try {
            reminderNotificationDispatcher.send(notification);
        } catch (RuntimeException e) {
            log.warn("PUSH appointment reminder {} to {} failed",
                    notification.reminderId(), notification.recipientUserId().value(), e);
        }
    }
}
