package com.healthify.guardian.platform.shared.infrastructure.i18n;

import org.jspecify.annotations.NullMarked;
import org.springframework.context.i18n.LocaleContextHolder;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Resolves message-bundle keys against the request's current locale.
 *
 * <p>Domain and application code across every bounded context raises exceptions whose
 * message is a bundle key (e.g. {@code "reminder.cannot.confirm"}) rather than free text,
 * following the same convention used for {@code messages.properties} / {@code messages_es.properties}.
 * This resolver is the single place that turns such a key into the localized sentence shown
 * to API clients, so no bounded context needs to duplicate {@link ResourceBundle} lookup logic.</p>
 */
@NullMarked
public final class MessageResolver {

    private static final String MESSAGES_BASENAME = "messages";

    /**
     * Java's default {@link ResourceBundle} lookup silently falls back to the JVM's platform
     * default locale (here, Windows' regional Spanish) before ever trying the base bundle,
     * whenever the requested locale has no exact-language file of its own. On a Spanish-locale
     * machine that means an English request would incorrectly resolve Spanish text instead of
     * falling back to {@code messages.properties}. This no-fallback control restricts candidate
     * locales to the requested locale's own chain (e.g. {@code en -> root}), so the platform's
     * default locale never interferes with the resolution.
     */
    private static final ResourceBundle.Control NO_LOCALE_FALLBACK_CONTROL =
            ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES);

    private MessageResolver() {
    }

    /**
     * Resolves a message key for the current locale, falling back to the given default
     * when the key is absent from the bundle (or the key itself when no default is given).
     *
     * @param key          the message-bundle key
     * @param defaultValue the value to use when the key is not found
     * @param args         positional arguments substituted into the resolved template
     * @return the localized, formatted message
     */
    public static String resolveOrDefault(String key, String defaultValue, Object... args) {
        try {
            var bundle = ResourceBundle.getBundle(
                    MESSAGES_BASENAME, LocaleContextHolder.getLocale(), NO_LOCALE_FALLBACK_CONTROL);
            if (!bundle.containsKey(key)) {
                return defaultValue;
            }
            return MessageFormat.format(bundle.getString(key), args);
        } catch (MissingResourceException ex) {
            return defaultValue;
        }
    }

    /**
     * Resolves a message key for the current locale, or {@code null} when the key is absent.
     * Useful for callers that need to try several candidate keys before falling back.
     *
     * @param key  the message-bundle key
     * @param args positional arguments substituted into the resolved template
     * @return the localized, formatted message, or null if the key was not found
     */
    public static String resolveOrNull(String key, Object... args) {
        return resolveOrDefault(key, null, args);
    }
}
