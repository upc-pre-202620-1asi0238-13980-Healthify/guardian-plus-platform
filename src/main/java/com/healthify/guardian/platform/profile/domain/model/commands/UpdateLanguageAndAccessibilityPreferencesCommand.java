package com.healthify.guardian.platform.profile.domain.model.commands;

import com.healthify.guardian.platform.profile.domain.model.valueobjects.FontScale;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.Language;

import java.util.UUID;

/**
 * Command to update language and accessibility preferences.
 *
 * @param userId               the referenced IAM user
 * @param language             selected application language
 * @param highContrastEnabled  whether high contrast is enabled
 * @param reduceMotionEnabled  whether interface motion is reduced
 * @param fontScale            selected text size
 */
public record UpdateLanguageAndAccessibilityPreferencesCommand(
        UUID userId,
        Language language,
        boolean highContrastEnabled,
        boolean reduceMotionEnabled,
        FontScale fontScale) {
}