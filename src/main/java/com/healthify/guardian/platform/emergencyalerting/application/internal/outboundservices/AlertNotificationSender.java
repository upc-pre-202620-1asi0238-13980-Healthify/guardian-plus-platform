package com.healthify.guardian.platform.emergencyalerting.application.internal.outboundservices;

import com.healthify.guardian.platform.emergencyalerting.application.acl.MobilityContextAcl;
import com.healthify.guardian.platform.emergencyalerting.application.acl.MobilityContextAcl.LastKnownLocation;
import com.healthify.guardian.platform.emergencyalerting.application.acl.ProfileContextAcl;
import com.healthify.guardian.platform.emergencyalerting.application.commandservices.AlertCommandService;
import com.healthify.guardian.platform.emergencyalerting.application.outboundservices.AlertNotification;
import com.healthify.guardian.platform.emergencyalerting.application.outboundservices.NotificationDispatcher;
import com.healthify.guardian.platform.emergencyalerting.application.outboundservices.NotificationPurpose;
import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.Alert;
import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.AlertChannelSetting;
import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.AlertSettings;
import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.EmergencyContact;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.RegisterDeliveryResultCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.entities.AlertDelivery;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertDeliveryId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertSourceType;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.DeliveryStatus;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.NotificationChannel;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.Severity;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.UserId;
import com.healthify.guardian.platform.emergencyalerting.domain.repositories.AlertChannelSettingRepository;
import com.healthify.guardian.platform.emergencyalerting.domain.repositories.AlertRepository;
import com.healthify.guardian.platform.emergencyalerting.domain.repositories.AlertSettingsRepository;
import com.healthify.guardian.platform.emergencyalerting.domain.repositories.EmergencyContactRepository;
import com.healthify.guardian.platform.shared.application.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Sends the notifications of an alert through {@link NotificationDispatcher} and records each
 * provider outcome on the alert.
 *
 * <p>Runs on its own executor, off the thread that dispatched the alert, so a slow provider never
 * delays persisting the alert or answering the request that triggered it.</p>
 */
@Slf4j
@Service
public class AlertNotificationSender {

    private final AlertRepository alertRepository;
    private final AlertSettingsRepository alertSettingsRepository;
    private final EmergencyContactRepository emergencyContactRepository;
    private final AlertChannelSettingRepository alertChannelSettingRepository;
    private final ProfileContextAcl profileContextAcl;
    private final MobilityContextAcl mobilityContextAcl;
    private final NotificationDispatcher notificationDispatcher;
    private final AlertCommandService alertCommandService;
    private final Clock clock;

    public AlertNotificationSender(
            AlertRepository alertRepository,
            AlertSettingsRepository alertSettingsRepository,
            EmergencyContactRepository emergencyContactRepository,
            AlertChannelSettingRepository alertChannelSettingRepository,
            ProfileContextAcl profileContextAcl,
            MobilityContextAcl mobilityContextAcl,
            NotificationDispatcher notificationDispatcher,
            AlertCommandService alertCommandService,
            Clock emergencyAlertingClock) {
        this.alertRepository = alertRepository;
        this.alertSettingsRepository = alertSettingsRepository;
        this.emergencyContactRepository = emergencyContactRepository;
        this.alertChannelSettingRepository = alertChannelSettingRepository;
        this.profileContextAcl = profileContextAcl;
        this.mobilityContextAcl = mobilityContextAcl;
        this.notificationDispatcher = notificationDispatcher;
        this.alertCommandService = alertCommandService;
        this.clock = emergencyAlertingClock;
    }

    /**
     * Sends the given, still pending deliveries of an alert and records whether each provider
     * accepted them.
     *
     * @param alertId     the dispatched alert
     * @param deliveryIds the deliveries just generated for one recipient level
     */
    @Async("emergencyAlertingNotificationExecutor")
    public void sendDeliveries(AlertId alertId, List<AlertDeliveryId> deliveryIds) {
        try {
            var alert = alertRepository.findById(alertId).orElse(null);
            if (alert == null) {
                return;
            }
            var pending = deliveryIds.stream()
                    .map(alert::findDelivery)
                    .flatMap(Optional::stream)
                    .filter(delivery -> delivery.getDeliveryStatus() == DeliveryStatus.PENDING)
                    .toList();
            var recipients = pending.stream().map(AlertDelivery::getRecipientUserId).collect(Collectors.toSet());
            var context = contextFor(alert, recipients);

            for (var delivery : pending) {
                var status = send(new AlertNotification(
                        NotificationPurpose.ALERT,
                        alert.getId(),
                        delivery.getId(),
                        alert.getCareRecipientProfileId(),
                        context.careRecipientName(),
                        alert.getSource().sourceType(),
                        alert.getSeverity(),
                        delivery.getRecipientUserId(),
                        delivery.getChannel(),
                        context.addressOf(delivery.getRecipientUserId(), delivery.getChannel()),
                        context.audible(),
                        context.location(),
                        null));
                registerResult(alert.getId(), delivery.getId(), status);
            }
        } catch (RuntimeException e) {
            log.error("Unexpected failure sending the deliveries of alert {}", alertId.value(), e);
        }
    }

    /**
     * Tells every other member notified of an alert that someone has already taken charge of it
     * (US25). These notices are informational, so they are never sent by SMS and are not recorded
     * as deliveries.
     *
     * @param alertId         the alert being responded to
     * @param responderUserId the member who took charge
     */
    @Async("emergencyAlertingNotificationExecutor")
    public void sendResponseClaimed(AlertId alertId, UserId responderUserId) {
        try {
            var alert = alertRepository.findById(alertId).orElse(null);
            if (alert == null) {
                return;
            }
            var channelsByRecipient = alert.getDeliveries().stream()
                    .filter(delivery -> !delivery.getRecipientUserId().equals(responderUserId))
                    .filter(delivery -> delivery.getChannel() != NotificationChannel.SMS)
                    .collect(Collectors.groupingBy(AlertDelivery::getRecipientUserId,
                            Collectors.mapping(AlertDelivery::getChannel, Collectors.toSet())));
            var context = contextFor(alert, channelsByRecipient.keySet());

            channelsByRecipient.forEach((recipient, channels) -> channels.forEach(channel -> send(new AlertNotification(
                    NotificationPurpose.RESPONSE_CLAIMED,
                    alert.getId(),
                    null,
                    alert.getCareRecipientProfileId(),
                    context.careRecipientName(),
                    alert.getSource().sourceType(),
                    alert.getSeverity(),
                    recipient,
                    channel,
                    context.addressOf(recipient, channel),
                    false,
                    null,
                    responderUserId))));
        } catch (RuntimeException e) {
            log.error("Unexpected failure announcing the responder of alert {}", alertId.value(), e);
        }
    }

    private DeliveryStatus send(AlertNotification notification) {
        try {
            return notificationDispatcher.send(notification);
        } catch (RuntimeException e) {
            log.warn("{} notification of alert {} to {} failed",
                    notification.channel(), notification.alertId().value(), notification.recipientUserId().value(), e);
            return DeliveryStatus.FAILED;
        }
    }

    private void registerResult(AlertId alertId, AlertDeliveryId deliveryId, DeliveryStatus status) {
        var result = alertCommandService.handle(
                new RegisterDeliveryResultCommand(alertId.value(), deliveryId.value(), status, clock.instant()));
        if (result instanceof Result.Failure<?, ?> failure) {
            log.warn("Outcome {} of delivery {} could not be recorded: {}", status, deliveryId.value(), failure.error());
        }
    }

    private NotificationContext contextFor(Alert alert, Collection<UserId> recipients) {
        var settings = alertSettingsRepository.findByCareRecipientProfileId(alert.getCareRecipientProfileId())
                .orElseGet(() -> new AlertSettings(alert.getCareRecipientProfileId()));
        var phones = emergencyContactRepository
                .findActiveByCareRecipientProfileIdOrderByPriority(alert.getCareRecipientProfileId()).stream()
                .collect(Collectors.toMap(EmergencyContact::getUserId, contact -> contact.getPhoneNumber().value()));
        var deviceTokens = alertChannelSettingRepository.findByUserIdIn(recipients).stream()
                .filter(setting -> setting.getChannel() == NotificationChannel.PUSH && setting.getDeviceToken() != null)
                .collect(Collectors.toMap(AlertChannelSetting::getUserId, AlertChannelSetting::getDeviceToken));
        var location = includesLocation(alert)
                ? mobilityContextAcl.findLastKnownLocation(alert.getCareRecipientProfileId()).orElse(null)
                : null;
        return new NotificationContext(
                profileContextAcl.findCareRecipientDisplayName(alert.getCareRecipientProfileId()).orElse(null),
                settings.allowsAudibleNotificationFor(alert.getSeverity()),
                location,
                phones,
                deviceTokens);
    }

    /** The location is only attached where it helps the Care Circle reach the Fragile Citizen. */
    private static boolean includesLocation(Alert alert) {
        return alert.getSeverity() == Severity.CRITICAL
                || alert.getSource().sourceType() == AlertSourceType.SAFE_ZONE_VIOLATION;
    }

    /** What every notification of one alert has in common, loaded once per batch. */
    private record NotificationContext(
            String careRecipientName,
            boolean audible,
            LastKnownLocation location,
            Map<UserId, String> phones,
            Map<UserId, String> deviceTokens) {

        String addressOf(UserId recipient, NotificationChannel channel) {
            return switch (channel) {
                case SMS -> phones.get(recipient);
                case PUSH -> deviceTokens.get(recipient);
                case IN_APP -> null;
            };
        }
    }
}
