package com.healthify.guardian.platform.healthmonitoring.interfaces.rest.transform;

import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.GenerateHealthReportCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.HealthReportType;
import com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources.GenerateHealthReportResource;

/**
 * Assembler converting a {@link GenerateHealthReportResource} into an on-demand {@link GenerateHealthReportCommand}.
 */
public final class GenerateHealthReportCommandFromResourceAssembler {

    private GenerateHealthReportCommandFromResourceAssembler() {
    }

    public static GenerateHealthReportCommand toCommandFromResource(GenerateHealthReportResource resource) {
        return new GenerateHealthReportCommand(
                resource.careRecipientProfileId(),
                resource.generatedByUserId(),
                HealthReportType.ON_DEMAND.name(),
                resource.periodStart(),
                resource.periodEnd());
    }
}
