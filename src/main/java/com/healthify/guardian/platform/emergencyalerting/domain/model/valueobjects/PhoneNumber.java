package com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects;

import java.util.regex.Pattern;

/**
 * Phone number of an emergency contact, used for the {@code SMS} channel.
 *
 * @param value the number in E.164 format, including the international prefix (e.g. {@code +51987654321})
 */
public record PhoneNumber(String value) {

    private static final Pattern E164 = Pattern.compile("^\\+[1-9]\\d{7,14}$");
    private static final String INVALID_MESSAGE_KEY = "phone-number.invalid";

    public PhoneNumber {
        if (value == null || !E164.matcher(value).matches()) {
            throw new IllegalArgumentException(INVALID_MESSAGE_KEY);
        }
    }
}
