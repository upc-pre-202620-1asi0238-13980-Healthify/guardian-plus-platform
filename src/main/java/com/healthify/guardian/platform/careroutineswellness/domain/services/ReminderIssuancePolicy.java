package com.healthify.guardian.platform.careroutineswellness.domain.services;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.Reminder;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.IssuanceOutcome;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderType;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.SleepWindow;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;

/**
 * Decides whether a reminder due for issuance should actually be issued or suppressed.
 *
 * <p>Business rule: a hydration reminder that falls inside the person under care's configured
 * sleep window is suppressed rather than issued, to avoid waking them up for water. Every other
 * reminder type, and hydration reminders outside the sleep window, are issued normally.</p>
 */
@Service
public class ReminderIssuancePolicy {

    private final ZoneId issuanceZone;

    public ReminderIssuancePolicy(ZoneId careRoutinesWellnessZoneId) {
        this.issuanceZone = careRoutinesWellnessZoneId;
    }

    /**
     * Determines the issuance outcome for a reminder that has just reached its scheduled time.
     *
     * @param reminder    the reminder due for issuance
     * @param currentTime the current time
     * @param sleepWindow the person under care's configured sleep window
     * @return {@code SUPPRESS} for a hydration reminder inside the sleep window, {@code ISSUE} otherwise
     */
    public IssuanceOutcome determineIssuanceOutcome(Reminder reminder, Instant currentTime, SleepWindow sleepWindow) {
        boolean suppressibleHydration = reminder.getType() == ReminderType.HYDRATION
                && sleepWindow.contains(currentTime, issuanceZone);
        return suppressibleHydration ? IssuanceOutcome.SUPPRESS : IssuanceOutcome.ISSUE;
    }
}
