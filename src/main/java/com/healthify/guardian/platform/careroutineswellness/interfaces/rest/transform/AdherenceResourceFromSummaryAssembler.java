package com.healthify.guardian.platform.careroutineswellness.interfaces.rest.transform;

import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.AdherenceSummary;
import com.healthify.guardian.platform.careroutineswellness.interfaces.rest.resources.AdherenceResource;

/**
 * Assembler that converts an {@link AdherenceSummary} into an {@link AdherenceResource}.
 */
public final class AdherenceResourceFromSummaryAssembler {

    private AdherenceResourceFromSummaryAssembler() {
    }

    public static AdherenceResource toResourceFromSummary(AdherenceSummary summary) {
        return new AdherenceResource(
                summary.type() == null ? null : summary.type().name(),
                summary.from(),
                summary.to(),
                summary.due(),
                summary.confirmed(),
                summary.percentage(),
                summary.days().stream()
                        .map(day -> new AdherenceResource.DailyAdherenceResource(
                                day.date(), day.due(), day.confirmed(), day.percentage()))
                        .toList());
    }
}
