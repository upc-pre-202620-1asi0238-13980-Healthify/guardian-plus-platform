package com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.entities;

import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.UserId;
import com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.converters.CareRecipientProfileIdPersistenceConverter;
import com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.converters.UserIdPersistenceConverter;
import com.healthify.guardian.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA persistence entity for emergency contacts.
 *
 * <p>A user can be a contact of a Fragile Citizen only once. Priorities are deliberately not
 * unique at the database level, so that reordering several contacts in one batch never trips over
 * a transient duplicate; the application service guarantees they end up consecutive.</p>
 */
@Entity
@Table(name = "emergency_contacts", uniqueConstraints = @UniqueConstraint(
        name = "uk_emergency_contacts_care_recipient_user",
        columnNames = {"care_recipient_profile_id", "user_id"}))
@Getter
@Setter
@NoArgsConstructor
public class EmergencyContactPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Convert(converter = CareRecipientProfileIdPersistenceConverter.class)
    @Column(name = "care_recipient_profile_id", nullable = false)
    private CareRecipientProfileId careRecipientProfileId;

    @Convert(converter = UserIdPersistenceConverter.class)
    @Column(name = "user_id", nullable = false)
    private UserId userId;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(nullable = false, length = 50)
    private String relationship;

    @Column(name = "phone_number", nullable = false, length = 16)
    private String phoneNumber;

    @Column(name = "priority_order", nullable = false)
    private Integer priorityOrder;

    @Column(nullable = false)
    private boolean active;
}
