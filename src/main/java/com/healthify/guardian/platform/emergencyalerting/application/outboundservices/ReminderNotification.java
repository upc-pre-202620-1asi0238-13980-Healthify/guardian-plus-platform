package com.healthify.guardian.platform.emergencyalerting.application.outboundservices;

import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.UserId;

import java.time.Instant;
import java.util.UUID;

/**
 * Everything a push provider needs to give one caregiver the preventive reminder of a medical appointment of
 * the Fragile Citizen they look after. Unlike an {@link AlertNotification}, it is informational: it does not
 * open an alert nor expect an acknowledgement.
 *
 * @param reminderId             the issued appointment reminder, as {@code Care Routines & Wellness} knows it
 * @param careRecipientProfileId the Fragile Citizen the appointment belongs to
 * @param careRecipientName      the Fragile Citizen's name, if known
 * @param title                  what the appointment is about, e.g. "Control de cardiología"
 * @param location               where it takes place, if known
 * @param scheduledTime          when it takes place
 * @param recipientUserId        the caregiver to notify
 * @param deviceToken            the caregiver's push device token; {@code null} if they registered none
 */
public record ReminderNotification(
        UUID reminderId,
        CareRecipientProfileId careRecipientProfileId,
        String careRecipientName,
        String title,
        String location,
        Instant scheduledTime,
        UserId recipientUserId,
        String deviceToken) {
}
