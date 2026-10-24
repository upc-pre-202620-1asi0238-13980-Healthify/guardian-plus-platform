package com.healthify.guardian.platform.profile.domain.model.commands;

import java.util.UUID;

/**
 * Command to end an active care relationship.
 *
 * @param careRelationshipId the relationship to end
 */
public record EndCareRelationshipCommand(
        UUID careRelationshipId) {
}