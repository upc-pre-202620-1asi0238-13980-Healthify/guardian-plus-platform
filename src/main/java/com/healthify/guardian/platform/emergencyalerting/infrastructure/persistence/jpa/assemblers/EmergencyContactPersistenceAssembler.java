package com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.assemblers;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.EmergencyContact;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.EmergencyContactId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.PhoneNumber;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.PriorityOrder;
import com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.entities.EmergencyContactPersistenceEntity;

/**
 * Static assembler between the {@link EmergencyContact} domain aggregate and its persistence entity.
 */
public final class EmergencyContactPersistenceAssembler {

    private EmergencyContactPersistenceAssembler() {
    }

    public static EmergencyContact toDomainFromPersistence(EmergencyContactPersistenceEntity entity) {
        if (entity == null) return null;
        var contact = new EmergencyContact();
        contact.setId(new EmergencyContactId(entity.getId()));
        contact.setCareRecipientProfileId(entity.getCareRecipientProfileId());
        contact.setUserId(entity.getUserId());
        contact.setDisplayName(entity.getDisplayName());
        contact.setRelationship(entity.getRelationship());
        contact.setPhoneNumber(new PhoneNumber(entity.getPhoneNumber()));
        contact.setPriorityOrder(new PriorityOrder(entity.getPriorityOrder()));
        contact.setActive(entity.isActive());
        return contact;
    }

    public static EmergencyContactPersistenceEntity toPersistenceFromDomain(EmergencyContact contact) {
        if (contact == null) return null;
        var entity = new EmergencyContactPersistenceEntity();
        entity.setId(contact.getId().value());
        entity.setCareRecipientProfileId(contact.getCareRecipientProfileId());
        entity.setUserId(contact.getUserId());
        entity.setDisplayName(contact.getDisplayName());
        entity.setRelationship(contact.getRelationship());
        entity.setPhoneNumber(contact.getPhoneNumber().value());
        entity.setPriorityOrder(contact.getPriorityOrder().value());
        entity.setActive(contact.isActive());
        return entity;
    }
}
