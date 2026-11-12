package com.healthify.guardian.platform.emergencyalerting.application.internal.outboundservices;

import com.healthify.guardian.platform.emergencyalerting.application.acl.ProfileContextAcl;
import com.healthify.guardian.platform.emergencyalerting.application.outboundservices.ReminderNotification;
import com.healthify.guardian.platform.emergencyalerting.application.outboundservices.ReminderNotificationDispatcher;
import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.AlertChannelSetting;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.DeliveryStatus;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.NotificationChannel;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.UserId;
import com.healthify.guardian.platform.emergencyalerting.domain.repositories.AlertChannelSettingRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests of {@link ReminderNotificationSender}.
 */
class ReminderNotificationSenderTest {

    private final ProfileContextAcl profileContextAcl = mock(ProfileContextAcl.class);
    private final AlertChannelSettingRepository alertChannelSettingRepository = mock(AlertChannelSettingRepository.class);
    private final ReminderNotificationDispatcher dispatcher = mock(ReminderNotificationDispatcher.class);
    private final ReminderNotificationSender sender =
            new ReminderNotificationSender(profileContextAcl, alertChannelSettingRepository, dispatcher);

    private final UUID reminder = UUID.randomUUID();
    private final CareRecipientProfileId careRecipient = new CareRecipientProfileId(UUID.randomUUID());
    private final Instant appointmentTime = Instant.parse("2026-11-13T15:30:00Z");

    @Test
    void everyCaregiverWithAPushTokenIsNotified() {
        var mother = new UserId(UUID.randomUUID());
        var son = new UserId(UUID.randomUUID());
        when(profileContextAcl.findActiveCaregivers(careRecipient)).thenReturn(List.of(mother, son));
        when(profileContextAcl.findCareRecipientDisplayName(careRecipient)).thenReturn(Optional.of("Ana Pérez"));
        when(alertChannelSettingRepository.findByUserIdIn(anyCollection())).thenReturn(List.of(
                pushSetting(mother, "token-mother", true),
                pushSetting(son, "token-son", true)));
        when(dispatcher.send(any())).thenReturn(DeliveryStatus.SENT);

        sendAppointmentReminder();

        var sent = ArgumentCaptor.forClass(ReminderNotification.class);
        verify(dispatcher, times(2)).send(sent.capture());
        assertThat(sent.getAllValues()).containsExactlyInAnyOrder(
                notification(mother, "token-mother"),
                notification(son, "token-son"));
    }

    @Test
    void caregiversWithoutAnEnabledPushTokenAreSkipped() {
        var withoutToken = new UserId(UUID.randomUUID());
        var disabled = new UserId(UUID.randomUUID());
        var reachable = new UserId(UUID.randomUUID());
        when(profileContextAcl.findActiveCaregivers(careRecipient)).thenReturn(List.of(withoutToken, disabled, reachable));
        when(profileContextAcl.findCareRecipientDisplayName(careRecipient)).thenReturn(Optional.of("Ana Pérez"));
        when(alertChannelSettingRepository.findByUserIdIn(anyCollection())).thenReturn(List.of(
                pushSetting(disabled, "token-disabled", false),
                pushSetting(reachable, "token-reachable", true)));

        sendAppointmentReminder();

        var sent = ArgumentCaptor.forClass(ReminderNotification.class);
        verify(dispatcher).send(sent.capture());
        assertThat(sent.getValue()).isEqualTo(notification(reachable, "token-reachable"));
    }

    @Test
    void nothingIsSentWhenThePersonHasNoCaregivers() {
        when(profileContextAcl.findActiveCaregivers(careRecipient)).thenReturn(List.of());

        sendAppointmentReminder();

        verify(dispatcher, never()).send(any());
    }

    @Test
    void aFailingProviderDoesNotStopTheOtherCaregivers() {
        var first = new UserId(UUID.randomUUID());
        var second = new UserId(UUID.randomUUID());
        when(profileContextAcl.findActiveCaregivers(careRecipient)).thenReturn(List.of(first, second));
        when(profileContextAcl.findCareRecipientDisplayName(careRecipient)).thenReturn(Optional.empty());
        when(alertChannelSettingRepository.findByUserIdIn(anyCollection())).thenReturn(List.of(
                pushSetting(first, "token-first", true),
                pushSetting(second, "token-second", true)));
        when(dispatcher.send(any())).thenThrow(new IllegalStateException("FCM down")).thenReturn(DeliveryStatus.SENT);

        sendAppointmentReminder();

        verify(dispatcher, times(2)).send(any());
    }

    private void sendAppointmentReminder() {
        sender.sendAppointmentReminder(reminder, careRecipient, "Control de cardiología", "Clínica San Pablo", appointmentTime);
    }

    private ReminderNotification notification(UserId recipient, String deviceToken) {
        return new ReminderNotification(reminder, careRecipient, "Ana Pérez", "Control de cardiología",
                "Clínica San Pablo", appointmentTime, recipient, deviceToken);
    }

    private static AlertChannelSetting pushSetting(UserId userId, String deviceToken, boolean enabled) {
        var setting = new AlertChannelSetting();
        setting.setUserId(userId);
        setting.setChannel(NotificationChannel.PUSH);
        setting.setEnabled(enabled);
        setting.setDeviceToken(deviceToken);
        return setting;
    }
}
