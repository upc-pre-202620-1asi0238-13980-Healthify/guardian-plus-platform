package com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates;

import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.RegisterVitalSignTypeCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.ReadingClassification;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignRange;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignTypeCode;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignTypeId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignValue;
import com.healthify.guardian.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

/**
 * Aggregate root acting as the catalog of vital sign types supported by the platform
 * (heart rate, systolic/diastolic pressure, oxygen saturation, temperature, respiratory rate),
 * each with its unique code and unit of measure.
 *
 * <p>Every type also carries its own reference ranges: the <b>normal range</b>, used as the default
 * clinical threshold when a care recipient has no personalized one, and the <b>physical limits</b>,
 * outside of which a reading cannot be real and is rejected as a sensor error.</p>
 */
@Getter
public class VitalSignType extends AbstractDomainAggregateRoot<VitalSignType> {

    private static final String NAME_INVALID_KEY = "vital-sign-type.name.invalid";
    private static final String UNIT_INVALID_KEY = "vital-sign-type.unit.invalid";
    private static final String NORMAL_RANGE_OUTSIDE_LIMITS_KEY = "vital-sign-type.normal-range.outside-physical-limits";

    private VitalSignTypeId id;
    private VitalSignTypeCode code;
    private String name;
    private String unit;
    private VitalSignRange normalRange;
    private VitalSignRange physicalLimits;

    /** Reconstitution constructor, used by the persistence assembler. */
    public VitalSignType() {
    }

    /**
     * Registers a new catalog entry.
     *
     * @param command the type definition
     */
    public VitalSignType(RegisterVitalSignTypeCommand command) {
        if (command.name() == null || command.name().isBlank()) {
            throw new IllegalArgumentException(NAME_INVALID_KEY);
        }
        if (command.unit() == null || command.unit().isBlank()) {
            throw new IllegalArgumentException(UNIT_INVALID_KEY);
        }
        this.id = VitalSignTypeId.generate();
        this.code = new VitalSignTypeCode(command.code());
        this.name = command.name().strip();
        this.unit = command.unit().strip();
        defineReferenceRanges(
                new VitalSignRange(command.normalMinimum(), command.normalMaximum()),
                new VitalSignRange(command.physicalMinimum(), command.physicalMaximum()));
    }

    /**
     * Sets the normal range and the physical limits of this type. The normal range must lie
     * within the physical limits.
     */
    public void defineReferenceRanges(VitalSignRange normalRange, VitalSignRange physicalLimits) {
        if (!physicalLimits.encloses(normalRange)) {
            throw new IllegalArgumentException(NORMAL_RANGE_OUTSIDE_LIMITS_KEY);
        }
        this.normalRange = normalRange;
        this.physicalLimits = physicalLimits;
    }

    public boolean hasReferenceRanges() {
        return normalRange != null && physicalLimits != null;
    }

    /**
     * Whether a reading can physically exist for this type. Types without reference ranges
     * accept any value.
     */
    public boolean isPhysicallyPossible(VitalSignValue value) {
        return physicalLimits == null || physicalLimits.contains(value);
    }

    /**
     * Whether a clinical range for a care recipient stays within what this type can physically measure.
     */
    public boolean allows(VitalSignRange clinicalRange) {
        return physicalLimits == null || physicalLimits.encloses(clinicalRange);
    }

    /**
     * Classifies a reading against the normal range of this type.
     */
    public ReadingClassification classify(VitalSignValue value) {
        return normalRange.classify(value);
    }

    /** Restores state from persistence. Used by the persistence assembler. */
    public void setId(VitalSignTypeId id) {
        this.id = id;
    }

    public void setCode(VitalSignTypeCode code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setNormalRange(VitalSignRange normalRange) {
        this.normalRange = normalRange;
    }

    public void setPhysicalLimits(VitalSignRange physicalLimits) {
        this.physicalLimits = physicalLimits;
    }
}
