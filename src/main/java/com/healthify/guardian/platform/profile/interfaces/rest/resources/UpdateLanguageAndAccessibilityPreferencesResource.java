package com.healthify.guardian.platform.profile.interfaces.rest.resources;

import com.healthify.guardian.platform.profile.domain.model.valueobjects.FontScale;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.Language;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload to update language and accessibility preferences.
 *
 * @param language            the application language
 * @param highContrastEnabled whether high contrast is enabled
 * @param reduceMotionEnabled whether reduced motion is enabled
 * @param fontScale           the preferred text size
 */
public record UpdateLanguageAndAccessibilityPreferencesResource(

        @NotNull(message = "{user-preferences.language.blank}")
        Language language,

        @NotNull(message = "{user-preferences.high-contrast-enabled.blank}")
        Boolean highContrastEnabled,

        @NotNull(message = "{user-preferences.reduce-motion-enabled.blank}")
        Boolean reduceMotionEnabled,

        @NotNull(message = "{user-preferences.font-scale.blank}")
        FontScale fontScale
) {
}