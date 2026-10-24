package com.healthify.guardian.platform.profile.infrastructure.persistence.jpa.entities;

import com.healthify.guardian.platform.profile.domain.model.valueobjects.UserId;
import com.healthify.guardian.platform.profile.infrastructure.persistence.jpa.converters.UserIdPersistenceConverter;
import com.healthify.guardian.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * JPA persistence entity for care recipient profiles.
 */
@Entity
@Table(name = "care_recipient_profiles", indexes = {
        @Index(
                name = "ix_care_recipient_profiles_created_by_user_id",
                columnList = "created_by_user_id")
})
@Getter
@Setter
@NoArgsConstructor
public class CareRecipientProfilePersistenceEntity
        extends AuditableAbstractPersistenceEntity {

    @Convert(converter = UserIdPersistenceConverter.class)
    @Column(name = "created_by_user_id", nullable = false)
    private UserId createdByUserId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "profile_image_url")
    private String profileImageUrl;
}