package com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates;

import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.AddEmergencyContactCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.events.EmergencyContactsChangedEvent;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.EmergencyContactChange;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.EmergencyContactId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.PhoneNumber;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.PriorityOrder;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.UserId;
import com.healthify.guardian.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

/**
 * Aggregate root registering a Care Circle member as an emergency contact of a Fragile Citizen,
 * with their position in the escalation chain.
 *
 * <p>Keeps a local copy of the contact's details (name, relationship and phone number) so that a
 * slow or unavailable {@code Profile} context never blocks an emergency dispatch; the copy is kept
 * in sync through Profile's care relationship integration events.</p>
 */
@Getter
public class EmergencyContact extends AbstractDomainAggregateRoot<EmergencyContact> {

    private static final int MAX_DISPLAY_NAME_LENGTH = 100;
    private static final int MAX_RELATIONSHIP_LENGTH = 50;
    private static final String DISPLAY_NAME_INVALID_MESSAGE_KEY = "emergency-contact.display-name.invalid";
    private static final String RELATIONSHIP_INVALID_MESSAGE_KEY = "emergency-contact.relationship.invalid";
    private static final String PHONE_NUMBER_INVALID_MESSAGE_KEY = "phone-number.invalid";
    private static final String PRIORITY_INVALID_MESSAGE_KEY = "priority-order.invalid";
    private static final String CANNOT_DEACTIVATE_MESSAGE_KEY = "emergency-contact.cannot.deactivate";

    private EmergencyContactId id;
    private CareRecipientProfileId careRecipientProfileId;
    private UserId userId;
    private String displayName;
    private String relationship;
    private PhoneNumber phoneNumber;
    private PriorityOrder priorityOrder;
    private boolean active;

    /** Reconstitution constructor, used by the persistence assembler. */
    public EmergencyContact() {
    }

    /** Registers a new, active emergency contact. */
    public EmergencyContact(AddEmergencyContactCommand command) {
        this.careRecipientProfileId = new CareRecipientProfileId(command.careRecipientProfileId());
        this.userId = new UserId(command.userId());
        this.displayName = requireText(command.displayName(), MAX_DISPLAY_NAME_LENGTH, DISPLAY_NAME_INVALID_MESSAGE_KEY);
        this.relationship = requireText(command.relationship(), MAX_RELATIONSHIP_LENGTH, RELATIONSHIP_INVALID_MESSAGE_KEY);
        this.phoneNumber = new PhoneNumber(command.phoneNumber());
        this.priorityOrder = new PriorityOrder(command.priorityOrder());
        this.id = EmergencyContactId.generate();
        this.active = true;
        registerDomainEvent(EmergencyContactsChangedEvent.from(this, EmergencyContactChange.ADDED));
    }

    /** Refreshes the local copy of the contact's details, e.g. after Profile reports a change. */
    public void updateContactDetails(String displayName, String relationship, PhoneNumber phoneNumber) {
        this.displayName = requireText(displayName, MAX_DISPLAY_NAME_LENGTH, DISPLAY_NAME_INVALID_MESSAGE_KEY);
        this.relationship = requireText(relationship, MAX_RELATIONSHIP_LENGTH, RELATIONSHIP_INVALID_MESSAGE_KEY);
        if (phoneNumber == null) {
            throw new IllegalArgumentException(PHONE_NUMBER_INVALID_MESSAGE_KEY);
        }
        this.phoneNumber = phoneNumber;
        registerDomainEvent(EmergencyContactsChangedEvent.from(this, EmergencyContactChange.DETAILS_UPDATED));
    }

    /** Moves this contact to another position of the escalation chain. */
    public void changePriority(PriorityOrder priorityOrder) {
        if (priorityOrder == null) {
            throw new IllegalArgumentException(PRIORITY_INVALID_MESSAGE_KEY);
        }
        if (priorityOrder.equals(this.priorityOrder)) {
            return;
        }
        this.priorityOrder = priorityOrder;
        registerDomainEvent(EmergencyContactsChangedEvent.from(this, EmergencyContactChange.REPRIORITIZED));
    }

    /** Puts a previously deactivated contact back into the escalation chain. */
    public void activate() {
        if (active) {
            return;
        }
        this.active = true;
        registerDomainEvent(EmergencyContactsChangedEvent.from(this, EmergencyContactChange.ACTIVATED));
    }

    /**
     * Removes this contact from future alert deliveries. Checking that the Fragile Citizen keeps at
     * least one active contact requires the rest of their contacts, so that rule is enforced by the
     * application service.
     */
    public void deactivate() {
        if (!active) {
            throw new IllegalStateException(CANNOT_DEACTIVATE_MESSAGE_KEY);
        }
        this.active = false;
        registerDomainEvent(EmergencyContactsChangedEvent.from(this, EmergencyContactChange.DEACTIVATED));
    }

    /** True if this contact is active and holds the first position of the escalation chain. */
    public boolean isPrimary() {
        return active && priorityOrder.isPrimary();
    }

    private static String requireText(String value, int maxLength, String messageKey) {
        if (value == null || value.isBlank() || value.strip().length() > maxLength) {
            throw new IllegalArgumentException(messageKey);
        }
        return value.strip();
    }

    /** Restores an identity and state from persistence. Used by the persistence assembler. */
    public void setId(EmergencyContactId id) {
        this.id = id;
    }

    public void setCareRecipientProfileId(CareRecipientProfileId careRecipientProfileId) {
        this.careRecipientProfileId = careRecipientProfileId;
    }

    public void setUserId(UserId userId) {
        this.userId = userId;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public void setPhoneNumber(PhoneNumber phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setPriorityOrder(PriorityOrder priorityOrder) {
        this.priorityOrder = priorityOrder;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
