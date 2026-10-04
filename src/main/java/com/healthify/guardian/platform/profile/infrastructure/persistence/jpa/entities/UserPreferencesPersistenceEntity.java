package com.healthify.guardian.platform.profile.infrastructure.persistence.jpa.entities;

import com.healthify.guardian.platform.profile.domain.model.valueobjects.FontScale;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.Language;
import com.healthify.guardian.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA persistence entity for user preferences.
 *
 * The inherited identifier represents the UserId because preferences
 * have a one-to-one relationship with a Guardian+ user.
 */
@Entity
@Table(name = "user_preferences")
@AttributeOverride(
        name = "id",
        column = @Column(name = "user_id", nullable = false))
@Getter
@Setter
@NoArgsConstructor
public class UserPreferencesPersistenceEntity
        extends AuditableAbstractPersistenceEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private Language language;

    @Column(name = "notifications_enabled", nullable = false)
    private boolean notificationsEnabled;

    @Column(name = "high_contrast_enabled", nullable = false)
    private boolean highContrastEnabled;

    @Column(name = "reduce_motion_enabled", nullable = false)
    private boolean reduceMotionEnabled;

    @Enumerated(EnumType.STRING)
    @Column(name = "font_scale", nullable = false, length = 20)
    private FontScale fontScale;
}