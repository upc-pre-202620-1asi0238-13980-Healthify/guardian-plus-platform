package com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.assemblers;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.Reminder;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.MedicationStockId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.RecurrenceRule;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderDetails;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderSeriesId;
import com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.entities.ReminderPersistenceEntity;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Static assembler between the {@link Reminder} domain aggregate and its persistence entity.
 *
 * <p>Rows written before reminders had details, recurrence or a series are reconstituted as one-off
 * reminders titled after their type, each one being its own series.</p>
 */
public final class ReminderPersistenceAssembler {

    private static final String DAYS_SEPARATOR = ",";

    private ReminderPersistenceAssembler() {
    }

    public static Reminder toDomainFromPersistence(ReminderPersistenceEntity entity) {
        if (entity == null) return null;
        var reminder = new Reminder();
        reminder.setId(new ReminderId(entity.getId()));
        reminder.setSeriesId(new ReminderSeriesId(entity.getSeriesId() != null ? entity.getSeriesId() : entity.getId()));
        reminder.setPersonUnderCareId(entity.getPersonUnderCareId());
        reminder.setType(entity.getType());
        reminder.setDetails(new ReminderDetails(
                entity.getTitle() != null ? entity.getTitle() : entity.getType().name(),
                entity.getDosage(), entity.getInstructions(), entity.getLocation(), entity.getDurationMinutes()));
        reminder.setScheduledTime(entity.getScheduledTime());
        reminder.setLeadTimeMinutes(entity.getLeadTimeMinutes() != null ? entity.getLeadTimeMinutes() : 0);
        reminder.setRecurrence(new RecurrenceRule(
                entity.getRecurrenceFrequency(), toDays(entity.getRecurrenceDaysOfWeek()), entity.getRecurrenceIntervalHours()));
        reminder.setMedicationStockId(entity.getMedicationStockId() == null ? null : new MedicationStockId(entity.getMedicationStockId()));
        reminder.setIssuedAt(entity.getIssuedAt());
        reminder.setConfirmedAt(entity.getConfirmedAt());
        reminder.setStatus(entity.getStatus());
        reminder.setReissueCount(entity.getReissueCount());
        return reminder;
    }

    public static ReminderPersistenceEntity toPersistenceFromDomain(Reminder reminder) {
        if (reminder == null) return null;
        var entity = new ReminderPersistenceEntity();
        entity.setId(reminder.getId().value());
        entity.setSeriesId(reminder.getSeriesId().value());
        entity.setPersonUnderCareId(reminder.getPersonUnderCareId());
        entity.setType(reminder.getType());
        var details = reminder.getDetails();
        entity.setTitle(details.title());
        entity.setDosage(details.dosage());
        entity.setInstructions(details.instructions());
        entity.setLocation(details.location());
        entity.setDurationMinutes(details.durationMinutes());
        entity.setScheduledTime(reminder.getScheduledTime());
        entity.setLeadTimeMinutes(reminder.getLeadTimeMinutes());
        entity.setNotifyAt(reminder.notifyAt());
        var recurrence = reminder.getRecurrence();
        entity.setRecurrenceFrequency(recurrence.frequency());
        entity.setRecurrenceDaysOfWeek(fromDays(recurrence));
        entity.setRecurrenceIntervalHours(recurrence.intervalHours());
        entity.setMedicationStockId(reminder.getMedicationStockId() == null ? null : reminder.getMedicationStockId().value());
        entity.setIssuedAt(reminder.getIssuedAt());
        entity.setConfirmedAt(reminder.getConfirmedAt());
        entity.setStatus(reminder.getStatus());
        entity.setReissueCount(reminder.getReissueCount());
        return entity;
    }

    private static Set<DayOfWeek> toDays(String days) {
        if (days == null || days.isBlank()) return null;
        return Arrays.stream(days.split(DAYS_SEPARATOR))
                .map(DayOfWeek::valueOf)
                .collect(Collectors.toSet());
    }

    private static String fromDays(RecurrenceRule recurrence) {
        if (recurrence.daysOfWeek().isEmpty()) return null;
        return recurrence.daysOfWeek().stream()
                .sorted()
                .map(DayOfWeek::name)
                .collect(Collectors.joining(DAYS_SEPARATOR));
    }
}
