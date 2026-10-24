package com.healthify.guardian.platform.profile.interfaces.rest.transform;

import com.healthify.guardian.platform.profile.domain.model.commands.UpdateLanguageAndAccessibilityPreferencesCommand;
import com.healthify.guardian.platform.profile.interfaces.rest.resources.UpdateLanguageAndAccessibilityPreferencesResource;

import java.util.UUID;

/**
 * Assembler to convert an
 * {@link UpdateLanguageAndAccessibilityPreferencesResource}
 * to an {@link UpdateLanguageAndAccessibilityPreferencesCommand}.
 */
public final class UpdateLanguageAndAccessibilityPreferencesCommandFromResourceAssembler {

    private UpdateLanguageAndAccessibilityPreferencesCommandFromResourceAssembler() {
    }

    public static UpdateLanguageAndAccessibilityPreferencesCommand toCommandFromResource(
            UUID userId,
            UpdateLanguageAndAccessibilityPreferencesResource resource) {

        return new UpdateLanguageAndAccessibilityPreferencesCommand(
                userId,
                resource.language(),
                resource.highContrastEnabled(),
                resource.reduceMotionEnabled(),
                resource.fontScale());
    }
}