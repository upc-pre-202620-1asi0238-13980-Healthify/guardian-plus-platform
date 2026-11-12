package com.healthify.guardian.platform.emergencyalerting.application.outboundservices;

import com.healthify.guardian.platform.emergencyalerting.application.acl.MobilityContextAcl.LastKnownLocation;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertDeliveryId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertSourceType;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.NotificationChannel;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.Severity;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.UserId;

/**
 * Everything a notification provider needs to notify one Care Circle member through one channel,
 * independent of the provider.
 *
 * @param purpose                why the member is notified
 * @param alertId                the alert the notification is about
 * @param deliveryId             the delivery being sent; {@code null} unless {@code purpose} is
 *                               {@code ALERT}
 * @param careRecipientProfileId the Fragile Citizen the alert is about
 * @param careRecipientName      the Fragile Citizen's name, if known
 * @param sourceType             the kind of signal that raised the alert
 * @param severity               the alert's severity
 * @param recipientUserId        the member to notify
 * @param channel                the channel to notify them through
 * @param address                the phone number ({@code SMS}) or device token ({@code PUSH});
 *                               {@code null} for {@code IN_APP} or if unknown
 * @param audible                whether the notification may make a sound (silent mode only lets
 *                               critical alerts through)
 * @param location               the Fragile Citizen's last known location, for critical and safe zone alerts
 * @param responderUserId        the member who took charge; only for {@code RESPONSE_CLAIMED}
 */
public record AlertNotification(
        NotificationPurpose purpose,
        AlertId alertId,
        AlertDeliveryId deliveryId,
        CareRecipientProfileId careRecipientProfileId,
        String careRecipientName,
        AlertSourceType sourceType,
        Severity severity,
        UserId recipientUserId,
        NotificationChannel channel,
        String address,
        boolean audible,
        LastKnownLocation location,
        UserId responderUserId) {
}
