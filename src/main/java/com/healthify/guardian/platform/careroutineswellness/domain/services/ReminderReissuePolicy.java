package com.healthify.guardian.platform.careroutineswellness.domain.services;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.Reminder;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderStatus;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/**
 * Decides whether an issued medication reminder requires reissuing because the person under
 * care has not confirmed it within the configured tolerance window.
 */
@Service
public class ReminderReissuePolicy {

    private final Duration tolerance;

    public ReminderReissuePolicy(
            @Value("${care-routines-wellness.reminder.reissue-tolerance-minutes:10}") long toleranceMinutes) {
        this.tolerance = Duration.ofMinutes(toleranceMinutes);
    }

    /**
     * Tells whether the given reminder must be reissued at the given time.
     *
     * <p>Only medication reminders are eligible for reissuing; any other issued reminder type
     * is left to lapse without a repeated notification.</p>
     *
     * @param reminder    the reminder to evaluate
     * @param currentTime the current time
     * @return true if the reminder is a medication reminder, is currently issued, and has
     *         been waiting for confirmation for at least the tolerance duration
     */
    public boolean requiresReissue(Reminder reminder, Instant currentTime) {
        if (reminder.getType() != ReminderType.MEDICATION || reminder.getStatus() != ReminderStatus.ISSUED) {
            return false;
        }
        var issuedAt = reminder.getIssuedAt();
        return issuedAt != null && !issuedAt.plus(tolerance).isAfter(currentTime);
    }
}
