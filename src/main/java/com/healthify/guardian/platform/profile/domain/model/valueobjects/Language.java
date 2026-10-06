package com.healthify.guardian.platform.profile.domain.model.valueobjects;

/**
 * Languages supported by Guardian+.
 */
public enum Language {

    SPANISH_LATIN_AMERICA("es-419"),
    ENGLISH_UNITED_STATES("en-US");

    private final String code;

    Language(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}