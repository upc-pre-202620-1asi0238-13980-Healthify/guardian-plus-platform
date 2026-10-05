package com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects;

import java.util.Locale;

/**
 * Vital signs monitored by the wearable (US01-US05). Each type carries its own clinical condition:
 * the <b>normal range</b>, the threshold every reading of that type is evaluated against, and the
 * <b>physical limits</b>, outside of which a reading cannot be real and is rejected as a sensor error.
 *
 * <p>Normal ranges are adult resting references. Blood pressure is split into its systolic and
 * diastolic components so each one is evaluated on its own.</p>
 */
public enum VitalSignType {

    HR("Heart rate", "bpm", VitalSignRange.of("60", "100"), VitalSignRange.of("20", "250")),
    BP_SYS("Systolic blood pressure", "mmHg", VitalSignRange.of("90", "140"), VitalSignRange.of("50", "260")),
    BP_DIA("Diastolic blood pressure", "mmHg", VitalSignRange.of("60", "90"), VitalSignRange.of("30", "160")),
    SPO2("Peripheral oxygen saturation", "%", VitalSignRange.of("92", "100"), VitalSignRange.of("0", "100")),
    TEMP("Body temperature", "°C", VitalSignRange.of("36.0", "37.5"), VitalSignRange.of("30.0", "43.0")),
    RESP_RATE("Respiratory rate", "rpm", VitalSignRange.of("12", "20"), VitalSignRange.of("4", "60"));

    private static final String INVALID_MESSAGE_KEY = "vital-sign.type.invalid";

    private final String displayName;
    private final String unit;
    private final VitalSignRange normalRange;
    private final VitalSignRange physicalLimits;

    VitalSignType(String displayName, String unit, VitalSignRange normalRange, VitalSignRange physicalLimits) {
        this.displayName = displayName;
        this.unit = unit;
        this.normalRange = normalRange;
        this.physicalLimits = physicalLimits;
    }

    /**
     * Resolves a type from its code (e.g. {@code hr}, {@code SPO2}), ignoring case and surrounding blanks.
     */
    public static VitalSignType fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException(INVALID_MESSAGE_KEY);
        }
        try {
            return valueOf(code.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(INVALID_MESSAGE_KEY);
        }
    }

    public String code() {
        return name();
    }

    public String displayName() {
        return displayName;
    }

    public String unit() {
        return unit;
    }

    public VitalSignRange normalRange() {
        return normalRange;
    }

    public VitalSignRange physicalLimits() {
        return physicalLimits;
    }

    /**
     * Classifies a reading against the normal range of this type.
     */
    public ReadingClassification classify(VitalSignValue value) {
        return normalRange.classify(value);
    }

    /**
     * Whether a reading can physically exist for this type.
     */
    public boolean isPhysicallyPossible(VitalSignValue value) {
        return physicalLimits.contains(value);
    }
}
