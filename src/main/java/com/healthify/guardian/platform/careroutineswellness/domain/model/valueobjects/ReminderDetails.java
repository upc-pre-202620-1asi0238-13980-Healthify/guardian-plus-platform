package com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects;

/**
 * Immutable description of what a reminder is about, as the family member entered it.
 *
 * <p>Which fields are relevant depends on the reminder type: {@code dosage} for medication,
 * {@code location} for medical appointments and {@code durationMinutes} for physical activity.
 * Only the title is mandatory.</p>
 *
 * @param title           short name shown to the person under care, e.g. "Losartán" or "Control de cardiología"
 * @param dosage          dose to take, e.g. "50 mg"
 * @param instructions    free-text indications, e.g. "Después del almuerzo"
 * @param location        where the appointment takes place
 * @param durationMinutes how long the activity lasts
 */
public record ReminderDetails(String title, String dosage, String instructions, String location, Integer durationMinutes) {

    private static final String TITLE_BLANK_MESSAGE_KEY = "reminder.title.blank";
    private static final String DURATION_INVALID_MESSAGE_KEY = "reminder.duration-minutes.invalid";

    public ReminderDetails {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(TITLE_BLANK_MESSAGE_KEY);
        }
        if (durationMinutes != null && durationMinutes <= 0) {
            throw new IllegalArgumentException(DURATION_INVALID_MESSAGE_KEY);
        }
        title = title.strip();
        dosage = blankToNull(dosage);
        instructions = blankToNull(instructions);
        location = blankToNull(location);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
