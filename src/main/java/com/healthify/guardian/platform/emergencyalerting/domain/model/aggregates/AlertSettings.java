package com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates;

import com.healthify.guardian.platform.emergencyalerting.domain.model.events.AlertSettingsUpdatedEvent;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AckTimeout;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertSettingsId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.Severity;
import com.healthify.guardian.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

/**
 * Aggregate root holding the alerting configuration of one Fragile Citizen: how long to wait for
 * the primary contact's acknowledgement, whether to escalate, whether critical alerts skip the
 * escalation chain, and silent mode. There is at most one per {@code CareRecipientProfileId}.
 */
@Getter
public class AlertSettings extends AbstractDomainAggregateRoot<AlertSettings> {

    private static final String CARE_RECIPIENT_INVALID_MESSAGE_KEY = "care-recipient-profile.id.invalid";
    private static final String INVALID_MESSAGE_KEY = "alert-settings.invalid";

    private AlertSettingsId id;
    private CareRecipientProfileId careRecipientProfileId;
    private AckTimeout primaryAckTimeout;
    private boolean escalationEnabled;
    private boolean silentModeEnabled;
    private boolean broadcastCriticalImmediately;

    /** Reconstitution constructor, used by the persistence assembler. */
    public AlertSettings() {
    }

    /**
     * Creates the default configuration: a 60 s acknowledgement timeout, escalation enabled,
     * critical alerts escalating like any other, and silent mode off.
     */
    public AlertSettings(CareRecipientProfileId careRecipientProfileId) {
        if (careRecipientProfileId == null) {
            throw new IllegalArgumentException(CARE_RECIPIENT_INVALID_MESSAGE_KEY);
        }
        this.id = AlertSettingsId.generate();
        this.careRecipientProfileId = careRecipientProfileId;
        this.primaryAckTimeout = AckTimeout.defaultTimeout();
        this.escalationEnabled = true;
        this.silentModeEnabled = false;
        this.broadcastCriticalImmediately = false;
    }

    /** Updates the acknowledgement timeout and the escalation behavior. */
    public void update(AckTimeout primaryAckTimeout, Boolean escalationEnabled, Boolean broadcastCriticalImmediately) {
        if (primaryAckTimeout == null || escalationEnabled == null || broadcastCriticalImmediately == null) {
            throw new IllegalArgumentException(INVALID_MESSAGE_KEY);
        }
        this.primaryAckTimeout = primaryAckTimeout;
        this.escalationEnabled = escalationEnabled;
        this.broadcastCriticalImmediately = broadcastCriticalImmediately;
        registerDomainEvent(AlertSettingsUpdatedEvent.from(this));
    }

    public void activateSilentMode() {
        if (silentModeEnabled) {
            return;
        }
        this.silentModeEnabled = true;
        registerDomainEvent(AlertSettingsUpdatedEvent.from(this));
    }

    public void deactivateSilentMode() {
        if (!silentModeEnabled) {
            return;
        }
        this.silentModeEnabled = false;
        registerDomainEvent(AlertSettingsUpdatedEvent.from(this));
    }

    /** Tells whether a notification of the given severity may be audible: always outside silent mode, only critical inside it. */
    public boolean allowsAudibleNotificationFor(Severity severity) {
        return !silentModeEnabled || severity.overridesSilentMode();
    }

    /** Restores an identity and state from persistence. Used by the persistence assembler. */
    public void setId(AlertSettingsId id) {
        this.id = id;
    }

    public void setCareRecipientProfileId(CareRecipientProfileId careRecipientProfileId) {
        this.careRecipientProfileId = careRecipientProfileId;
    }

    public void setPrimaryAckTimeout(AckTimeout primaryAckTimeout) {
        this.primaryAckTimeout = primaryAckTimeout;
    }

    public void setEscalationEnabled(boolean escalationEnabled) {
        this.escalationEnabled = escalationEnabled;
    }

    public void setSilentModeEnabled(boolean silentModeEnabled) {
        this.silentModeEnabled = silentModeEnabled;
    }

    public void setBroadcastCriticalImmediately(boolean broadcastCriticalImmediately) {
        this.broadcastCriticalImmediately = broadcastCriticalImmediately;
    }
}
