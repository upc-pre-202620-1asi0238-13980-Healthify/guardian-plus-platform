package com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RecurrenceRuleTest {

    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    // Thursday 2026-10-08 14:00 in Lima
    private static final Instant THURSDAY_2PM = Instant.parse("2026-10-08T19:00:00Z");

    @Test
    void oneOffReminderHasNoNextOccurrence() {
        assertThat(RecurrenceRule.once().nextOccurrenceAfter(THURSDAY_2PM, THURSDAY_2PM, LIMA)).isEmpty();
    }

    @Test
    void dailyReminderRepeatsNextDayAtTheSameLocalTime() {
        var rule = new RecurrenceRule(RecurrenceFrequency.DAILY, null, null);

        assertThat(rule.nextOccurrenceAfter(THURSDAY_2PM, THURSDAY_2PM, LIMA))
                .contains(Instant.parse("2026-10-09T19:00:00Z"));
    }

    @Test
    void weeklyReminderJumpsToTheNextSelectedDay() {
        var rule = new RecurrenceRule(RecurrenceFrequency.WEEKLY, Set.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY), null);

        assertThat(rule.nextOccurrenceAfter(THURSDAY_2PM, THURSDAY_2PM, LIMA))
                .contains(Instant.parse("2026-10-12T19:00:00Z"));
    }

    @Test
    void hourlyReminderRepeatsEveryInterval() {
        assertThat(RecurrenceRule.everyHours(2).nextOccurrenceAfter(THURSDAY_2PM, THURSDAY_2PM, LIMA))
                .contains(Instant.parse("2026-10-08T21:00:00Z"));
    }

    @Test
    void skipsOccurrencesAlreadyInThePast() {
        var threeDaysLater = Instant.parse("2026-10-11T20:00:00Z");
        var rule = new RecurrenceRule(RecurrenceFrequency.DAILY, null, null);

        assertThat(rule.nextOccurrenceAfter(THURSDAY_2PM, threeDaysLater, LIMA))
                .contains(Instant.parse("2026-10-12T19:00:00Z"));
    }

    @Test
    void weeklyRuleNeedsDaysAndHourlyRuleNeedsAValidInterval() {
        assertThatThrownBy(() -> new RecurrenceRule(RecurrenceFrequency.WEEKLY, Set.of(), null))
                .hasMessage("reminder.recurrence.days-of-week.required");
        assertThatThrownBy(() -> RecurrenceRule.everyHours(0))
                .hasMessage("reminder.recurrence.interval-hours.invalid");
    }

    @Test
    void dropsFieldsThatDoNotApplyToTheFrequency() {
        var rule = new RecurrenceRule(RecurrenceFrequency.DAILY, Set.of(DayOfWeek.MONDAY), 3);

        assertThat(rule.daysOfWeek()).isEmpty();
        assertThat(rule.intervalHours()).isNull();
    }
}
