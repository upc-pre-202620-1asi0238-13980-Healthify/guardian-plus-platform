package com.healthify.guardian.platform.careroutineswellness.interfaces.rest.transform;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.ActivityLogEntry;
import com.healthify.guardian.platform.careroutineswellness.interfaces.rest.resources.ActivityLogEntryResource;

/**
 * Assembler that converts an {@link ActivityLogEntry} domain aggregate into an {@link ActivityLogEntryResource}.
 */
public final class ActivityLogEntryResourceFromEntityAssembler {

    private ActivityLogEntryResourceFromEntityAssembler() {
    }

    public static ActivityLogEntryResource toResourceFromEntity(ActivityLogEntry entry) {
        return new ActivityLogEntryResource(
                entry.getId().value(),
                entry.getType().name(),
                entry.getOccurredAt(),
                entry.getDurationMinutes(),
                entry.getSteps(),
                entry.getInactiveMinutes());
    }
}
