package com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates;

import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.AssignWearableDeviceCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.DeviceStatus;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.DeviceType;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.SerialNumber;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.WearableDeviceId;
import com.healthify.guardian.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.time.Instant;
import java.util.Locale;

/**
 * Aggregate root registering the physical wearable device assigned to a care recipient and
 * governing its assignment lifecycle.
 */
@Getter
public class WearableDevice extends AbstractDomainAggregateRoot<WearableDevice> {

    private static final String DEVICE_TYPE_INVALID_KEY = "wearable-device.device-type.invalid";
    private static final String CANNOT_DEACTIVATE_KEY = "wearable-device.cannot.deactivate";

    private WearableDeviceId id;
    private CareRecipientProfileId careRecipientProfileId;
    private SerialNumber serialNumber;
    private DeviceType deviceType;
    private DeviceStatus status;
    private Instant assignedAt;
    private Instant createdAt;
    private Instant updatedAt;

    /** Reconstitution constructor, used by the persistence assembler. */
    public WearableDevice() {
    }

    /**
     * Assigns a new device to a care recipient.
     *
     * @param command the assignment data
     */
    public WearableDevice(AssignWearableDeviceCommand command) {
        var now = Instant.now();
        this.id = WearableDeviceId.generate();
        this.careRecipientProfileId = new CareRecipientProfileId(command.careRecipientProfileId());
        this.serialNumber = new SerialNumber(command.serialNumber());
        this.deviceType = parseDeviceType(command.deviceType());
        this.status = DeviceStatus.ASSIGNED;
        this.assignedAt = now;
        this.createdAt = now;
        this.updatedAt = now;
    }

    /** Takes an assigned device out of service; it stops being accepted as a telemetry source. */
    public void deactivate() {
        if (status != DeviceStatus.ASSIGNED) {
            throw new IllegalStateException(CANNOT_DEACTIVATE_KEY);
        }
        this.status = DeviceStatus.INACTIVE;
        this.updatedAt = Instant.now();
    }

    public boolean isAssigned() {
        return status == DeviceStatus.ASSIGNED;
    }

    /** Whether the device may report readings for the given care recipient. */
    public boolean canReportFor(CareRecipientProfileId careRecipientProfileId) {
        return isAssigned() && this.careRecipientProfileId.equals(careRecipientProfileId);
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

    public void setStatus(DeviceStatus status) {
        this.status = status;
    }

    public void setAssignedAt(Instant assignedAt) {
        this.assignedAt = assignedAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
