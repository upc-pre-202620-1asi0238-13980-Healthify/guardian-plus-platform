package com.healthify.guardian.platform.careroutineswellness.application.internal.queryservices;

import com.healthify.guardian.platform.careroutineswellness.application.queryservices.HydrationPlanQueryService;
import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.HydrationPlan;
import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.Reminder;
import com.healthify.guardian.platform.careroutineswellness.domain.model.queries.GetHydrationPlanByPersonUnderCareIdQuery;
import com.healthify.guardian.platform.careroutineswellness.domain.model.queries.GetHydrationProgressQuery;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.HydrationProgress;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderStatus;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderType;
import com.healthify.guardian.platform.careroutineswellness.domain.repositories.HydrationPlanRepository;
import com.healthify.guardian.platform.careroutineswellness.domain.repositories.ReminderRepository;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.Comparator;
import java.util.Optional;

/**
 * Application service that resolves hydration plan read queries.
 *
 * <p>Progress is derived from the person's hydration reminders: each confirmed one is a glass of water,
 * and the earliest still-scheduled one is the next reminder.</p>
 */
@Service
public class HydrationPlanQueryServiceImpl implements HydrationPlanQueryService {

    private final HydrationPlanRepository hydrationPlanRepository;
    private final ReminderRepository reminderRepository;
    private final ZoneId zone;

    public HydrationPlanQueryServiceImpl(
            HydrationPlanRepository hydrationPlanRepository,
            ReminderRepository reminderRepository,
            ZoneId careRoutinesWellnessZoneId) {
        this.hydrationPlanRepository = hydrationPlanRepository;
        this.reminderRepository = reminderRepository;
        this.zone = careRoutinesWellnessZoneId;
    }

    @Override
    public Optional<HydrationPlan> handle(GetHydrationPlanByPersonUnderCareIdQuery query) {
        return hydrationPlanRepository.findByPersonUnderCareId(query.personUnderCareId());
    }

    @Override
    public HydrationProgress handle(GetHydrationProgressQuery query) {
        var from = query.day().atStartOfDay(zone).toInstant();
        var to = query.day().plusDays(1).atStartOfDay(zone).toInstant();
        var glasses = (int) reminderRepository
                .findByPersonUnderCareId(query.personUnderCareId(), from, to, ReminderType.HYDRATION).stream()
                .filter(reminder -> reminder.getStatus() == ReminderStatus.CONFIRMED)
                .count();
        var nextReminderAt = reminderRepository
                .findActiveByPersonUnderCareIdAndType(query.personUnderCareId(), ReminderType.HYDRATION).stream()
                .filter(reminder -> reminder.getStatus() == ReminderStatus.SCHEDULED)
                .map(Reminder::notifyAt)
                .min(Comparator.naturalOrder())
                .orElse(null);
        return new HydrationProgress(query.day(), glasses, nextReminderAt);
    }
}
