package com.healthify.guardian.platform.iam.domain.model.valueobjects;

/**
 * Stable id Google gives a Google account (the {@code sub} claim of its ID tokens). Unlike the e-mail, it never
 * changes, so it is how a linked account is found on later Google sign-ins.
 *
 * @param value the subject, as Google sends it
 */
public record GoogleSubject(String value) {

    private static final int MAX_LENGTH = 255;
    private static final String INVALID_MESSAGE_KEY = "user-account.google-subject.invalid";

    public GoogleSubject {
        if (value == null || value.isBlank() || value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(INVALID_MESSAGE_KEY);
        }
    }
}
