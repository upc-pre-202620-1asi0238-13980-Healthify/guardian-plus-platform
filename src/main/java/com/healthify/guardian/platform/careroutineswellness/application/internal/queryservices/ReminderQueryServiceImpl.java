package com.healthify.guardian.platform.careroutineswellness.application.internal.queryservices;

import com.healthify.guardian.platform.careroutineswellness.application.queryservices.ReminderQueryService;
import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.Reminder;
import com.healthify.guardian.platform.careroutineswellness.domain.model.queries.GetReminderAdherenceQuery;
import com.healthify.guardian.platform.careroutineswellness.domain.model.queries.GetReminderStatusByIdQuery;
import com.healthify.guardian.platform.careroutineswellness.domain.model.queries.GetRemindersByPersonUnderCareIdQuery;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.AdherenceSummary;
import com.healthify.guardian.platform.careroutineswellness.domain.repositories.ReminderRepository;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

/**
 * Application service that resolves reminder read queries.
 */
@Service
public class ReminderQueryServiceImpl implements ReminderQueryService {

    private final ReminderRepository reminderRepository;
    private final ZoneId zone;

    public ReminderQueryServiceImpl(
            ReminderRepository reminderRepository,
            ZoneId careRoutinesWellnessZoneId) {
        this.reminderRepository = reminderRepository;
        this.zone = careRoutinesWellnessZoneId;
    }

    @Override
    public Optional<Reminder> handle(GetReminderStatusByIdQuery query) {
        return reminderRepository.findById(query.reminderId());
    }

    @Override
    public List<Reminder> handle(GetRemindersByPersonUnderCareIdQuery query) {
        return reminderRepository.findByPersonUnderCareId(query.personUnderCareId(), query.from(), query.to(), query.type());
    }

    @Override
    public AdherenceSummary handle(GetReminderAdherenceQuery query) {
        var from = query.from().atStartOfDay(zone).toInstant();
        var to = query.to().plusDays(1).atStartOfDay(zone).toInstant();
        var reminders = reminderRepository.findByPersonUnderCareId(query.personUnderCareId(), from, to, query.type());
        return AdherenceSummary.from(reminders, query.type(), query.from(), query.to(), zone);
    }
}
