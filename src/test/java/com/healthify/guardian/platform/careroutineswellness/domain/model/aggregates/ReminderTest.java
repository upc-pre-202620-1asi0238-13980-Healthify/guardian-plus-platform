package com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates;

import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.ScheduleReminderCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.events.ReminderConfirmedEvent;
import com.healthify.guardian.platform.careroutineswellness.domain.model.events.ReminderMissedEvent;
import com.healthify.guardian.platform.careroutineswellness.domain.model.events.ReminderScheduledEvent;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.RecurrenceFrequency;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderStatus;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderType;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReissueTolerance;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.SleepWindow;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReminderTest {

    private static final UUID PERSON = UUID.randomUUID();
    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private static final Instant TWO_PM = Instant.parse("2026-10-08T19:00:00Z");
    private static final SleepWindow NIGHT = new SleepWindow(LocalTime.of(22, 0), LocalTime.of(7, 0));
    private static final ReissueTolerance TEN_MINUTES = new ReissueTolerance(Duration.ofMinutes(10));

    private static ScheduleReminderCommand dailyLosartan(UUID stockId) {
        return new ScheduleReminderCommand(PERSON, ReminderType.MEDICATION, TWO_PM, " Losartán ", "50 mg",
                "Después del almuerzo", null, null, null, stockId, RecurrenceFrequency.DAILY, null, null);
    }

    @Test
    void schedulingKeepsTheDetailsEnteredByTheFamily() {
        var reminder = new Reminder(dailyLosartan(null));

        assertThat(reminder.getStatus()).isEqualTo(ReminderStatus.SCHEDULED);
        assertThat(reminder.getDetails().title()).isEqualTo("Losartán");
        assertThat(reminder.getDetails().dosage()).isEqualTo("50 mg");
        assertThat(reminder.getDetails().instructions()).isEqualTo("Después del almuerzo");
        assertThat(reminder.notifyAt()).isEqualTo(TWO_PM);
        assertThat(reminder.domainEvents()).singleElement().isInstanceOf(ReminderScheduledEvent.class);
    }

    @Test
    void appointmentIsNotifiedLeadTimeBeforeItHappens() {
        var appointment = new Reminder(new ScheduleReminderCommand(PERSON, ReminderType.APPOINTMENT, TWO_PM,
                "Control de cardiología", null, null, "Clínica San Gabriel", null, 60, null, null, null, null));

        assertThat(appointment.notifyAt()).isEqualTo(TWO_PM.minusSeconds(3600));
        assertThat(appointment.getDetails().location()).isEqualTo("Clínica San Gabriel");
    }

    @Test
    void rejectsABlankTitleAndAStockLinkedToANonMedicationReminder() {
        assertThatThrownBy(() -> new Reminder(new ScheduleReminderCommand(PERSON, ReminderType.HYDRATION, TWO_PM,
                " ", null, null, null, null, null, null, null, null, null)))
                .hasMessage("reminder.title.blank");
        assertThatThrownBy(() -> new Reminder(new ScheduleReminderCommand(PERSON, ReminderType.HYDRATION, TWO_PM,
                "Agua", null, null, null, null, null, UUID.randomUUID(), null, null, null)))
                .hasMessage("reminder.medication-stock-id.not-medication");
    }

    @Test
    void confirmationRecordsWhenAndCarriesTheLinkedStock() {
        var stockId = UUID.randomUUID();
        var reminder = new Reminder(dailyLosartan(stockId));
        reminder.issue(TWO_PM, NIGHT, LIMA, true);
        reminder.clearDomainEvents();

        reminder.confirm();

        assertThat(reminder.getConfirmedAt()).isNotNull();
        assertThat(reminder.domainEvents()).singleElement()
                .isInstanceOfSatisfying(ReminderConfirmedEvent.class, event -> {
                    assertThat(event.medicationStockId().value()).isEqualTo(stockId);
                    assertThat(event.title()).isEqualTo("Losartán");
                });
    }

    @Test
    void anUnconfirmedReminderCanBeClosedAsMissedButNotBeforeItWasIssued() {
        var reminder = new Reminder(dailyLosartan(null));
        assertThatThrownBy(reminder::markAsMissed).hasMessage("reminder.cannot.miss");

        reminder.issue(TWO_PM, NIGHT, LIMA, true);
        reminder.reissue();
        reminder.clearDomainEvents();
        reminder.markAsMissed();

        assertThat(reminder.getStatus()).isEqualTo(ReminderStatus.MISSED);
        assertThat(reminder.domainEvents()).singleElement().isInstanceOf(ReminderMissedEvent.class);
    }

    @Test
    void nextOccurrenceBelongsToTheSameSeries() {
        var reminder = new Reminder(dailyLosartan(null));

        var next = reminder.nextOccurrence(TWO_PM, LIMA).orElseThrow();

        assertThat(next.getId()).isNotEqualTo(reminder.getId());
        assertThat(next.getSeriesId()).isEqualTo(reminder.getSeriesId());
        assertThat(next.getDetails()).isEqualTo(reminder.getDetails());
        assertThat(next.getScheduledTime()).isEqualTo(Instant.parse("2026-10-09T19:00:00Z"));
        assertThat(next.getStatus()).isEqualTo(ReminderStatus.SCHEDULED);
    }

    @Test
    void oneOffReminderHasNoNextOccurrence() {
        var reminder = new Reminder(new ScheduleReminderCommand(PERSON, ReminderType.PHYSICAL_ACTIVITY, TWO_PM,
                "Caminata suave", null, null, null, 10, null, null, null, null, null));

        assertThat(reminder.nextOccurrence(TWO_PM, LIMA)).isEmpty();
        assertThat(reminder.getDetails().durationMinutes()).isEqualTo(10);
    }

    @Test
    void onlyAnIssuedMedicationReminderBecomesOverdueAfterTheTolerance() {
        var reminder = new Reminder(dailyLosartan(null));
        reminder.issue(TWO_PM, NIGHT, LIMA, true);

        assertThat(reminder.isOverdueForReissue(TWO_PM.plusSeconds(9 * 60), TEN_MINUTES)).isFalse();
        assertThat(reminder.isOverdueForReissue(TWO_PM.plusSeconds(10 * 60), TEN_MINUTES)).isTrue();

        reminder.reissue();
        assertThat(reminder.isOverdueForReissue(TWO_PM.plusSeconds(3600), TEN_MINUTES)).isFalse();

        var activity = new Reminder(new ScheduleReminderCommand(PERSON, ReminderType.PHYSICAL_ACTIVITY, TWO_PM,
                "Caminata suave", null, null, null, 10, null, null, null, null, null));
        activity.issue(TWO_PM, NIGHT, LIMA, true);
        assertThat(activity.isOverdueForReissue(TWO_PM.plusSeconds(3600), TEN_MINUTES)).isFalse();
    }

    @Test
    void hydrationIsSuppressedAtNightOnlyWhenTheSleepWindowIsRespected() {
        var elevenPm = Instant.parse("2026-10-09T04:00:00Z");
        var respected = hydrationAt(elevenPm);
        var ignored = hydrationAt(elevenPm);
        var medication = new Reminder(dailyLosartan(null));

        respected.issue(elevenPm, NIGHT, LIMA, true);
        ignored.issue(elevenPm, NIGHT, LIMA, false);
        medication.issue(elevenPm, NIGHT, LIMA, true);

        assertThat(respected.getStatus()).isEqualTo(ReminderStatus.SUPPRESSED);
        assertThat(respected.hasReachedPerson()).isFalse();
        assertThat(ignored.getStatus()).isEqualTo(ReminderStatus.ISSUED);
        assertThat(medication.getStatus()).isEqualTo(ReminderStatus.ISSUED);
        assertThat(medication.hasReachedPerson()).isTrue();
    }

    private static Reminder hydrationAt(Instant scheduledTime) {
        return new Reminder(new ScheduleReminderCommand(PERSON, ReminderType.HYDRATION, scheduledTime, "Hidratación",
                null, null, null, null, 0, null, RecurrenceFrequency.HOURLY, null, 2));
    }
}
