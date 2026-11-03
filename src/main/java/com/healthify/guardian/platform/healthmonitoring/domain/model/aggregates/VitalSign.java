package com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates;

import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.DetectVitalSignsCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.events.VitalSignsDetectedEvent;
import com.healthify.guardian.platform.healthmonitoring.domain.model.events.VitalSignsEmittedEvent;
import com.healthify.guardian.platform.healthmonitoring.domain.model.events.VitalSignsThresholdsEvaluatedEvent;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignType;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignValue;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.WearableDeviceId;
import com.healthify.guardian.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.time.Instant;

/**
 * Aggregate root representing the capture of a single vital sign type of a care recipient at a
 * given instant (one row, one metric). Its lifecycle follows the Design-Level EventStorming:
 * it is detected, then emitted for live consumption, then evaluated against the normal range of its
 * {@link VitalSignType}.
 */
@Getter
public class VitalSign extends AbstractDomainAggregateRoot<VitalSign> {

    private static final String MEASURED_AT_INVALID_KEY = "vital-sign.measured-at.invalid";
    private static final String RECEIVED_AT_INVALID_KEY = "vital-sign.received-at.invalid";
    private static final String MEASURED_AFTER_RECEIVED_KEY = "vital-sign.measured-at.after-received-at";
    private static final String ALREADY_EMITTED_KEY = "vital-sign.already-emitted";
    private static final String NOT_EMITTED_KEY = "vital-sign.not-emitted";
    private static final String OUTSIDE_PHYSICAL_LIMITS_KEY = "vital-sign.value.outside-physical-limits";

    private VitalSignId id;
    private WearableDeviceId wearableDeviceId;
    private CareRecipientProfileId careRecipientProfileId;
    private VitalSignType vitalSignType;
    private VitalSignValue value;
    private Instant measuredAt;
    private Instant receivedAt;
    private Instant emittedAt;

    /** Reconstitution constructor, used by the persistence assembler. */
    public VitalSign() {
    }

    /**
     * Detects a new reading. Registers {@link VitalSignsDetectedEvent}. A value outside the physical
     * limits of its type cannot be real and is rejected as a sensor error.
     *
     * @param command the reading sent by the wearable device
     */
    public VitalSign(DetectVitalSignsCommand command) {
        if (command.measuredAt() == null) {
            throw new IllegalArgumentException(MEASURED_AT_INVALID_KEY);
        }
        if (command.receivedAt() == null) {
            throw new IllegalArgumentException(RECEIVED_AT_INVALID_KEY);
        }
        if (command.measuredAt().isAfter(command.receivedAt())) {
            throw new IllegalArgumentException(MEASURED_AFTER_RECEIVED_KEY);
        }
        this.id = VitalSignId.generate();
        this.wearableDeviceId = new WearableDeviceId(command.wearableDeviceId());
        this.careRecipientProfileId = new CareRecipientProfileId(command.careRecipientProfileId());
        this.vitalSignType = VitalSignType.fromCode(command.vitalSignType());
        this.value = new VitalSignValue(command.value());
        if (!vitalSignType.isPhysicallyPossible(value)) {
            throw new IllegalArgumentException(OUTSIDE_PHYSICAL_LIMITS_KEY);
        }
        this.measuredAt = command.measuredAt();
        this.receivedAt = command.receivedAt();
        registerDomainEvent(new VitalSignsDetectedEvent(id, careRecipientProfileId, vitalSignType, measuredAt));
    }

    /**
     * Publishes the reading for live consumption. Registers {@link VitalSignsEmittedEvent}.
     *
     * @param now the emission instant
     */
    public void emit(Instant now) {
        if (isEmitted()) {
            throw new IllegalStateException(ALREADY_EMITTED_KEY);
        }
        this.emittedAt = now;
        registerDomainEvent(new VitalSignsEmittedEvent(id, careRecipientProfileId, vitalSignType, now));
    }

    /**
     * Evaluates the reading against the normal range of its vital sign type.
     * Registers {@link VitalSignsThresholdsEvaluatedEvent}.
     *
     * @param now the evaluation instant
     * @return {@code true} when the reading falls outside the normal range
     */
    public boolean evaluateThresholds(Instant now) {
        if (!isEmitted()) {
            throw new IllegalStateException(NOT_EMITTED_KEY);
        }
        var classification = vitalSignType.classify(value);
        var hasDeviation = classification.isOutOfRange();
        registerDomainEvent(new VitalSignsThresholdsEvaluatedEvent(
                id, careRecipientProfileId, vitalSignType, classification, hasDeviation, now));
        return hasDeviation;
    }

    public boolean isEmitted() {
        return emittedAt != null;
    }

    /** Restores state from persistence. Used by the persistence assembler. */
    public void setId(VitalSignId id) {
        this.id = id;
    }

    public void setWearableDeviceId(WearableDeviceId wearableDeviceId) {
        this.wearableDeviceId = wearableDeviceId;
    }

    public void setCareRecipientProfileId(CareRecipientProfileId careRecipientProfileId) {
        this.careRecipientProfileId = careRecipientProfileId;
    }

    public void setVitalSignType(VitalSignType vitalSignType) {
        this.vitalSignType = vitalSignType;
    }

    public void setValue(VitalSignValue value) {
        this.value = value;
    }

    public void setMeasuredAt(Instant measuredAt) {
        this.measuredAt = measuredAt;
    }

    public void setReceivedAt(Instant receivedAt) {
        this.receivedAt = receivedAt;
    }

    public void setEmittedAt(Instant emittedAt) {
        this.emittedAt = emittedAt;
    }
}
