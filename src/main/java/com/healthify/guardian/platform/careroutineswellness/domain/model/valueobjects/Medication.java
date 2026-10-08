package com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects;

/**
 * Immutable description of the medication a {@code MedicationStock} keeps track of.
 *
 * @param name   commercial or generic name, e.g. "Losartán"
 * @param dosage strength of each dose, e.g. "50 mg"
 */
public record Medication(String name, String dosage) {

    private static final String NAME_BLANK_MESSAGE_KEY = "medication-stock.medication-name.blank";

    public Medication {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(NAME_BLANK_MESSAGE_KEY);
        }
        name = name.strip();
        dosage = dosage == null || dosage.isBlank() ? null : dosage.strip();
    }

    /**
     * Tells whether this medication is the one referred to by the given name, ignoring case and
     * surrounding whitespace.
     *
     * @param otherName the name to compare against
     * @return true if both names designate the same medication
     */
    public boolean isNamed(String otherName) {
        return otherName != null && name.equalsIgnoreCase(otherName.strip());
    }
}
