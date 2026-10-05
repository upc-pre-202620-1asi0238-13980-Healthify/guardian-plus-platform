package com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates;

import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.RegisterVitalSignTypeCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignTypeCode;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignTypeId;
import com.healthify.guardian.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

/**
 * Aggregate root acting as the catalog of vital sign types supported by the platform
 * (heart rate, systolic/diastolic pressure, oxygen saturation, temperature, respiratory rate),
 * each with its unique code and unit of measure.
 */
@Getter
public class VitalSignType extends AbstractDomainAggregateRoot<VitalSignType> {

    private static final String NAME_INVALID_KEY = "vital-sign-type.name.invalid";
    private static final String UNIT_INVALID_KEY = "vital-sign-type.unit.invalid";

    private VitalSignTypeId id;
    private VitalSignTypeCode code;
    private String name;
    private String unit;

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
}
