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

/**
 * JPA persistence entity for user profiles.
 */
@Entity
@Table(name = "user_profiles", indexes = {
        @Index(name = "ix_user_profiles_user_id", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
public class UserProfilePersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Convert(converter = UserIdPersistenceConverter.class)
    @Column(name = "user_id", nullable = false)
    private UserId userId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "profile_image_url")
    private String profileImageUrl;
}