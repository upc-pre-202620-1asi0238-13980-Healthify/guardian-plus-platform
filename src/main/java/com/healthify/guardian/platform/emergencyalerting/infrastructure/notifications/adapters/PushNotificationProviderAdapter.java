package com.healthify.guardian.platform.emergencyalerting.infrastructure.notifications.adapters;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.healthify.guardian.platform.emergencyalerting.application.outboundservices.AlertNotification;
import com.healthify.guardian.platform.emergencyalerting.application.outboundservices.NotificationPurpose;
import com.healthify.guardian.platform.emergencyalerting.application.outboundservices.ReminderNotification;
import com.healthify.guardian.platform.emergencyalerting.application.outboundservices.ReminderNotificationDispatcher;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.DeliveryStatus;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.NotificationChannel;
import com.healthify.guardian.platform.emergencyalerting.infrastructure.notifications.ChannelNotificationAdapter;
import com.healthify.guardian.platform.emergencyalerting.infrastructure.notifications.PushNotificationTexts;
import com.healthify.guardian.platform.emergencyalerting.infrastructure.notifications.PushNotificationTexts.PushContent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.ZoneId;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * {@code PUSH} channel: anti-corruption layer towards Firebase Cloud Messaging. Sends each notification to the
 * member's device token through the FCM HTTP v1 API ({@code POST /v1/projects/{project}/messages:send}),
 * authenticated with the Firebase service account. The text comes from {@link PushNotificationTexts}; the data
 * payload lets the app open the alert, and silent mode uses a channel the app keeps without sound.
 *
 * <p>It also sends the preventive reminders of care routines ({@link ReminderNotificationDispatcher}), such as an
 * upcoming medical appointment, through a reminders channel of their own so the app never shows them as alerts.</p>
 *
 * <p><b>Simulated</b> when {@code emergency-alerting.notifications.push.fcm.service-account} is not set: the
 * notification is only written to the log. Either way it fails when the member registered no device token,
 * exactly as FCM would.</p>
 */
@Slf4j
@Component
public class PushNotificationProviderAdapter implements ChannelNotificationAdapter, ReminderNotificationDispatcher {

    /** Android notification channels the mobile app creates; silent mode lets only critical alerts sound. */
    static final String ALERTS_CHANNEL_ID = "guardian_alerts";
    static final String SILENT_ALERTS_CHANNEL_ID = "guardian_alerts_silent";
    /** Android notification channel the mobile app creates for preventive reminders (e.g. medical appointments). */
    static final String REMINDERS_CHANNEL_ID = "guardian_reminders";
    /** Value of the {@code kind} data field that tells the app a push is an appointment reminder, not an alert. */
    static final String APPOINTMENT_REMINDER_KIND = "APPOINTMENT_REMINDER";

    private static final String FCM_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";

    /** The Firebase project to send through and a source of short-lived OAuth access tokens for it. */
    record FcmAccount(String projectId, Supplier<String> accessTokens) {
    }

    private final RestClient restClient;
    private final FcmAccount account;
    private final ZoneId zone;

    @Autowired
    public PushNotificationProviderAdapter(
            RestClient.Builder restClientBuilder,
            @Value("${emergency-alerting.notifications.push.fcm.api-url:https://fcm.googleapis.com}") String apiUrl,
            @Value("${emergency-alerting.notifications.push.fcm.service-account:}") String serviceAccountBase64,
            @Value("${emergency-alerting.zone-id:America/Lima}") String zoneId) {
        this(restClientBuilder.clone().requestFactory(timeouts()), apiUrl, loadAccount(serviceAccountBase64),
                ZoneId.of(zoneId));
    }

    PushNotificationProviderAdapter(RestClient.Builder restClientBuilder, String apiUrl, FcmAccount account, ZoneId zone) {
        this.restClient = restClientBuilder.clone().baseUrl(apiUrl).build();
        this.account = account;
        this.zone = zone;
        if (account == null) {
            log.warn("emergency-alerting.notifications.push.fcm.service-account is not set: push notifications are only logged, not sent");
        }
    }

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.PUSH;
    }

    @Override
    public DeliveryStatus send(AlertNotification notification) {
        if (notification.address() == null) {
            log.warn("PUSH notification of alert {} to {} failed: no device token registered",
                    notification.alertId().value(), notification.recipientUserId().value());
            return DeliveryStatus.FAILED;
        }
        var content = PushNotificationTexts.of(notification);
        if (account == null) {
            log.info("[simulated PUSH] {} {} {} alert {} to {} (audible={}): {}",
                    notification.purpose(), notification.severity(), notification.sourceType(),
                    notification.alertId().value(), notification.recipientUserId().value(), notification.audible(),
                    content.title());
            return DeliveryStatus.SENT;
        }
        try {
            restClient.post()
                    .uri("/v1/projects/{projectId}/messages:send", account.projectId())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + account.accessTokens().get())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("message", message(notification, content)))
                    .retrieve()
                    .toBodilessEntity();
            return DeliveryStatus.SENT;
        } catch (RestClientResponseException e) {
            // 404 UNREGISTERED: the app was uninstalled, or signed out and deleted its token
            log.warn("FCM rejected the PUSH notification of alert {} to {}: {} {}",
                    notification.alertId().value(), notification.recipientUserId().value(),
                    e.getStatusCode().value(), e.getResponseBodyAsString());
            return DeliveryStatus.FAILED;
        } catch (RestClientException | IllegalStateException e) {
            log.error("PUSH notification of alert {} to {} could not be sent: {}",
                    notification.alertId().value(), notification.recipientUserId().value(), e.getMessage());
            return DeliveryStatus.FAILED;
        }
    }

    @Override
    public DeliveryStatus send(ReminderNotification notification) {
        if (notification.deviceToken() == null) {
            log.warn("PUSH appointment reminder {} to {} failed: no device token registered",
                    notification.reminderId(), notification.recipientUserId().value());
            return DeliveryStatus.FAILED;
        }
        var content = PushNotificationTexts.of(notification, zone);
        if (account == null) {
            log.info("[simulated PUSH] APPOINTMENT_REMINDER {} to {}: {} - {}",
                    notification.reminderId(), notification.recipientUserId().value(), content.title(), content.body());
            return DeliveryStatus.SENT;
        }
        try {
            restClient.post()
                    .uri("/v1/projects/{projectId}/messages:send", account.projectId())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + account.accessTokens().get())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("message", reminderMessage(notification, content)))
                    .retrieve()
                    .toBodilessEntity();
            return DeliveryStatus.SENT;
        } catch (RestClientResponseException e) {
            log.warn("FCM rejected the PUSH appointment reminder {} to {}: {} {}",
                    notification.reminderId(), notification.recipientUserId().value(),
                    e.getStatusCode().value(), e.getResponseBodyAsString());
            return DeliveryStatus.FAILED;
        } catch (RestClientException | IllegalStateException e) {
            log.error("PUSH appointment reminder {} to {} could not be sent: {}",
                    notification.reminderId(), notification.recipientUserId().value(), e.getMessage());
            return DeliveryStatus.FAILED;
        }
    }

    private static Map<String, Object> reminderMessage(ReminderNotification notification, PushContent content) {
        var reminderId = notification.reminderId().toString();
        return Map.of(
                "token", notification.deviceToken(),
                "notification", Map.of("title", content.title(), "body", content.body()),
                // FCM data values must be strings; "kind" tells the app this is not an alert
                "data", Map.of(
                        "kind", APPOINTMENT_REMINDER_KIND,
                        "reminderId", reminderId,
                        "careRecipientProfileId", notification.careRecipientProfileId().value().toString()),
                "android", Map.of(
                        "priority", "HIGH",
                        "notification", Map.of(
                                "channel_id", REMINDERS_CHANNEL_ID,
                                // the same reminder replaces its previous notification instead of piling up
                                "tag", reminderId)));
    }

    private static Map<String, Object> message(AlertNotification notification, PushContent content) {
        var alertId = notification.alertId().value().toString();
        return Map.of(
                "token", notification.address(),
                "notification", Map.of("title", content.title(), "body", content.body()),
                // FCM data values must be strings
                "data", Map.of(
                        "alertId", alertId,
                        "careRecipientProfileId", notification.careRecipientProfileId().value().toString(),
                        "purpose", notification.purpose().name(),
                        "sourceType", notification.sourceType().name(),
                        "severity", notification.severity().name(),
                        "audible", String.valueOf(notification.audible())),
                "android", Map.of(
                        "priority", notification.purpose() == NotificationPurpose.ALERT ? "HIGH" : "NORMAL",
                        "notification", Map.of(
                                "channel_id", notification.audible() ? ALERTS_CHANNEL_ID : SILENT_ALERTS_CHANNEL_ID,
                                // the same alert replaces its previous notification instead of piling up
                                "tag", alertId)));
    }

    private static FcmAccount loadAccount(String serviceAccountBase64) {
        if (serviceAccountBase64 == null || serviceAccountBase64.isBlank()) {
            return null;
        }
        try {
            var json = Base64.getDecoder().decode(serviceAccountBase64.strip());
            var credentials = ServiceAccountCredentials.fromStream(new ByteArrayInputStream(json));
            var scoped = credentials.createScoped(List.of(FCM_SCOPE));
            return new FcmAccount(credentials.getProjectId(), () -> {
                try {
                    // cached until it is about to expire, so most notifications reuse the same token
                    scoped.refreshIfExpired();
                    return scoped.getAccessToken().getTokenValue();
                } catch (IOException e) {
                    throw new IllegalStateException("no FCM access token: " + e.getMessage(), e);
                }
            });
        } catch (IOException | IllegalArgumentException e) {
            log.error("emergency-alerting.notifications.push.fcm.service-account is not a valid Base64 service account JSON: {}",
                    e.getMessage());
            return null;
        }
    }

    private static SimpleClientHttpRequestFactory timeouts() {
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(10));
        return requestFactory;
    }
}
