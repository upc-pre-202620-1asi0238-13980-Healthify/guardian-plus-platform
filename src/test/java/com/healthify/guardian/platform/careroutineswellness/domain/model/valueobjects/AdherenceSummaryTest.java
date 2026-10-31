package com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.Reminder;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.ScheduleReminderCommand;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AdherenceSummaryTest {

    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private static final SleepWindow NIGHT = new SleepWindow(LocalTime.of(22, 0), LocalTime.of(7, 0));
    private static final UUID PERSON = UUID.randomUUID();

    private static Reminder dose(String scheduledTime) {
        return new Reminder(new ScheduleReminderCommand(PERSON, ReminderType.MEDICATION, Instant.parse(scheduledTime),
                "Metformina", null, null, null, null, null, null, null, null, null));
    }

    private static Reminder issued(String scheduledTime) {
        var reminder = dose(scheduledTime);
        reminder.issue(reminder.getScheduledTime(), NIGHT, LIMA, true);
        return reminder;
    }

    private static Reminder confirmed(String scheduledTime) {
        var reminder = issued(scheduledTime);
        reminder.confirm();
        return reminder;
    }

    @Test
    void countsOnlyRemindersThatReachedThePersonDayByDay() {
        var cancelled = dose("2026-10-07T13:00:00Z");
        cancelled.cancel();
        var reminders = List.of(
                confirmed("2026-10-07T13:00:00Z"),
                confirmed("2026-10-07T19:00:00Z"),
                issued("2026-10-08T01:00:00Z"),       // 20:00 on the 7th in Lima
                confirmed("2026-10-08T13:00:00Z"),
                dose("2026-10-08T19:00:00Z"),          // still scheduled
                cancelled);

        var summary = AdherenceSummary.from(reminders, ReminderType.MEDICATION,
                LocalDate.of(2026, 10, 7), LocalDate.of(2026, 10, 8), LIMA);

        assertThat(summary.due()).isEqualTo(4);
        assertThat(summary.confirmed()).isEqualTo(3);
        assertThat(summary.percentage()).isEqualTo(75);
        assertThat(summary.days()).hasSize(2);
        assertThat(summary.days().get(0).due()).isEqualTo(3);
        assertThat(summary.days().get(0).percentage()).isEqualTo(67);
        assertThat(summary.days().get(1).percentage()).isEqualTo(100);
    }

    @Test
    void anEmptyDayHasZeroPercent() {
        var summary = AdherenceSummary.from(List.of(), null, LocalDate.of(2026, 10, 8), LocalDate.of(2026, 10, 8), LIMA);

        assertThat(summary.days()).singleElement().satisfies(day -> {
            assertThat(day.due()).isZero();
            assertThat(day.percentage()).isZero();
        });
    }
}
