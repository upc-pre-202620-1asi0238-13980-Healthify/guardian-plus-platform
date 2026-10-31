package com.healthify.guardian.platform.healthmonitoring.domain.model.commands;

import java.util.UUID;

/**
 * Command to compile the automatic weekly health summary of a care recipient.
 */
public record CompileWeeklySummaryCommand(UUID careRecipientProfileId) {
}
