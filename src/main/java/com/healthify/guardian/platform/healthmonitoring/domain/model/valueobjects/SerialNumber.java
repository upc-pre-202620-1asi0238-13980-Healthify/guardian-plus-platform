package com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects;

/**
 * Unique factory serial number of a wearable device.
 *
 * @param value the serial number, trimmed
 */
public record SerialNumber(String value) {

    private static final String INVALID_MESSAGE_KEY = "wearable-device.serial-number.invalid";

    public SerialNumber {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(INVALID_MESSAGE_KEY);
        }
        value = value.strip();
    }
}
