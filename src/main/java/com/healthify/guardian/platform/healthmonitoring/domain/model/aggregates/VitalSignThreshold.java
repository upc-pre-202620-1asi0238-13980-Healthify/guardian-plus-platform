package com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates;

import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.DefineVitalSignThresholdCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.ReadingClassification;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignThresholdId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignTypeId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignValue;
import com.healthify.guardian.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Aggregate root configuring, per care recipient and vital sign type, the valid clinical range
 * and how many consecutive out-of-range readings confirm an anomaly (Tolerance Rule).
 */
@Getter
public class VitalSignThreshold extends AbstractDomainAggregateRoot<VitalSignThreshold> {

    private static final String RANGE_INVALID_KEY = "vital-sign-threshold.range.invalid";
    private static final String CONSECUTIVE_HITS_INVALID_KEY = "vital-sign-threshold.consecutive-hits.invalid";
    private static final String ALREADY_ACTIVE_KEY = "vital-sign-threshold.already-active";
    private static final String ALREADY_INACTIVE_KEY = "vital-sign-threshold.already-inactive";

    private VitalSignThresholdId id;
    private CareRecipientProfileId careRecipientProfileId;
    private VitalSignTypeId vitalSignTypeId;
    private BigDecimal minimumValue;
    private BigDecimal maximumValue;
    private Integer requiredConsecutiveHits;
    private Boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    /** Reconstitution constructor, used by the persistence assembler. */
    public VitalSignThreshold() {
    }

    /**
     * Defines a new active threshold.
     *
     * @param command the clinical range definition
     */
    public VitalSignThreshold(DefineVitalSignThresholdCommand command) {
        validate(command.minimumValue(), command.maximumValue(), command.requiredConsecutiveHits());
        var now = Instant.now();
        this.id = VitalSignThresholdId.generate();
        this.careRecipientProfileId = new CareRecipientProfileId(command.careRecipientProfileId());
        this.vitalSignTypeId = new VitalSignTypeId(command.vitalSignTypeId());
        this.minimumValue = command.minimumValue();
        this.maximumValue = command.maximumValue();
        this.requiredConsecutiveHits = command.requiredConsecutiveHits();
        this.active = true;
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * Redefines the clinical range of an existing threshold and puts it back in force.
     */
    public void redefine(BigDecimal minimumValue, BigDecimal maximumValue, Integer requiredConsecutiveHits) {
        validate(minimumValue, maximumValue, requiredConsecutiveHits);
        this.minimumValue = minimumValue;
        this.maximumValue = maximumValue;
        this.requiredConsecutiveHits = requiredConsecutiveHits;
        this.active = true;
        this.updatedAt = Instant.now();
    }

    /**
     * Classifies a reading against this clinical range; both bounds are inclusive.
     */
    public ReadingClassification classify(VitalSignValue value) {
        if (value.value().compareTo(minimumValue) < 0) {
            return ReadingClassification.BELOW_RANGE;
        }
        if (value.value().compareTo(maximumValue) > 0) {
            return ReadingClassification.ABOVE_RANGE;
        }
        return ReadingClassification.WITHIN_RANGE;
    }

    public boolean isExceededBy(VitalSignValue value) {
        return classify(value).isOutOfRange();
    }

    public void activate() {
        if (Boolean.TRUE.equals(active)) {
            throw new IllegalStateException(ALREADY_ACTIVE_KEY);
        }
        this.active = true;
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        if (!Boolean.TRUE.equals(active)) {
            throw new IllegalStateException(ALREADY_INACTIVE_KEY);
        }
        this.active = false;
        this.updatedAt = Instant.now();
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(active);
    }

    private static void validate(BigDecimal minimumValue, BigDecimal maximumValue, Integer requiredConsecutiveHits) {
        if (minimumValue == null || maximumValue == null || minimumValue.compareTo(maximumValue) > 0) {
            throw new IllegalArgumentException(RANGE_INVALID_KEY);
        }
        if (requiredConsecutiveHits == null || requiredConsecutiveHits < 1) {
            throw new IllegalArgumentException(CONSECUTIVE_HITS_INVALID_KEY);
        }
    }

    /** Restores state from persistence. Used by the persistence assembler. */
    public void setId(VitalSignThresholdId id) {
        this.id = id;
    }

    public void setCareRecipientProfileId(CareRecipientProfileId careRecipientProfileId) {
        this.careRecipientProfileId = careRecipientProfileId;
    }

    public void setVitalSignTypeId(VitalSignTypeId vitalSignTypeId) {
        this.vitalSignTypeId = vitalSignTypeId;
    }

    public void setMinimumValue(BigDecimal minimumValue) {
        this.minimumValue = minimumValue;
    }

    public void setMaximumValue(BigDecimal maximumValue) {
        this.maximumValue = maximumValue;
    }

    public void setRequiredConsecutiveHits(Integer requiredConsecutiveHits) {
        this.requiredConsecutiveHits = requiredConsecutiveHits;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
