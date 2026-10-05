package com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates;

import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.LinkWearableDeviceCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.events.WearableDeviceLinkedEvent;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.DeviceType;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.SerialNumber;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.WearableDeviceId;
import com.healthify.guardian.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.time.Instant;
import java.util.Locale;

/**
 * Aggregate root registering the physical wearable device linked to a care recipient. Only a linked
 * device is accepted as the source of that care recipient's vital signs.
 */
@Getter
public class WearableDevice extends AbstractDomainAggregateRoot<WearableDevice> {

    private static final String DEVICE_TYPE_INVALID_KEY = "wearable-device.device-type.invalid";

    private WearableDeviceId id;
    private CareRecipientProfileId careRecipientProfileId;
    private SerialNumber serialNumber;
    private DeviceType deviceType;
    private Instant linkedAt;
    private Instant createdAt;
    private Instant updatedAt;

    /** Reconstitution constructor, used by the persistence assembler. */
    public WearableDevice() {
    }

    /**
     * Links a new device to a care recipient. Registers {@link WearableDeviceLinkedEvent}.
     *
     * @param command the link data
     */
    public WearableDevice(LinkWearableDeviceCommand command) {
        var now = Instant.now();
        this.id = WearableDeviceId.generate();
        this.careRecipientProfileId = new CareRecipientProfileId(command.careRecipientProfileId());
        this.serialNumber = new SerialNumber(command.serialNumber());
        this.deviceType = parseDeviceType(command.deviceType());
        this.linkedAt = now;
        this.createdAt = now;
        this.updatedAt = now;
        registerDomainEvent(new WearableDeviceLinkedEvent(id, careRecipientProfileId, serialNumber, linkedAt));
    }

    /** Whether the device may report readings for the given care recipient. */
    public boolean canReportFor(CareRecipientProfileId careRecipientProfileId) {
        return this.careRecipientProfileId.equals(careRecipientProfileId);
    }

    private static DeviceType parseDeviceType(String deviceType) {
        if (deviceType == null) {
            throw new IllegalArgumentException(DEVICE_TYPE_INVALID_KEY);
        }
        try {
            return DeviceType.valueOf(deviceType.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(DEVICE_TYPE_INVALID_KEY);
        }
    }

    /** Restores state from persistence. Used by the persistence assembler. */
    public void setId(WearableDeviceId id) {
        this.id = id;
    }

    public void setCareRecipientProfileId(CareRecipientProfileId careRecipientProfileId) {
        this.careRecipientProfileId = careRecipientProfileId;
    }

    public void setSerialNumber(SerialNumber serialNumber) {
        this.serialNumber = serialNumber;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public void setLinkedAt(Instant linkedAt) {
        this.linkedAt = linkedAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
