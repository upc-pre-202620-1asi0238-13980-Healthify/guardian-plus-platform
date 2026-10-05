package com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects;

/**
 * Unique catalog code of a vital sign type, e.g. {@code HR}, {@code BP_SYS}, {@code SPO2}.
 * Stored trimmed and upper-cased so lookups do not depend on how the code was typed.
 *
 * @param value the catalog code
 */
public record VitalSignTypeCode(String value) {

    private static final String INVALID_MESSAGE_KEY = "vital-sign-type.code.invalid";

    public VitalSignTypeCode {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(INVALID_MESSAGE_KEY);
        }
        value = value.strip().toUpperCase();
    }
}
