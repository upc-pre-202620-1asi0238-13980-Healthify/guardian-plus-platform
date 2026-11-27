package com.healthify.guardian.platform.emergencyalerting.application.outboundservices;

/**
 * Why a Care Circle member is being notified.
 */
public enum NotificationPurpose {
    /** The member must react to an alert; backed by an {@code AlertDelivery}. */
    ALERT,
    /** Another member has already taken charge of responding to an alert the member was notified about. */
    RESPONSE_CLAIMED,
    /** The Fragile Citizen cancelled a detected fall from their wristband, so nobody needs to go (US10). */
    ALERT_DISMISSED
}
