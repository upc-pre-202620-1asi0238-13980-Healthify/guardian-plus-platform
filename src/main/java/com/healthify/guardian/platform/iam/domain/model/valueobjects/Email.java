package com.healthify.guardian.platform.iam.domain.model.valueobjects;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * E-mail address that identifies a user account. It is the only login identifier: there is no username.
 *
 * @param value the address, trimmed and lower-cased
 */
public record Email(String value) {

    // Practical subset of RFC 5322: dot-atom local part and a dotted domain. The full grammar also allows
    // comments and quoted strings that mail providers and the mobile app never produce.
    private static final Pattern FORMAT = Pattern.compile(
            "^[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*"
                    + "@([A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?\\.)+[A-Za-z]{2,}$");
    private static final int MAX_LENGTH = 254;
    private static final String INVALID_MESSAGE_KEY = "user-account.email.invalid";

    public Email {
        if (value == null) {
            throw new IllegalArgumentException(INVALID_MESSAGE_KEY);
        }
        value = value.strip().toLowerCase(Locale.ROOT);
        if (value.length() > MAX_LENGTH || !FORMAT.matcher(value).matches()) {
            throw new IllegalArgumentException(INVALID_MESSAGE_KEY);
        }
    }

    /**
     * Tells whether the given raw address refers to this same e-mail, ignoring case and surrounding spaces.
     *
     * @param other the raw address to compare
     * @return true if both addresses are the same
     */
    public boolean matches(String other) {
        return other != null && value.equals(other.strip().toLowerCase(Locale.ROOT));
    }
}
