package com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.embeddables;

import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertSourceType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Groups the {@code AlertSource} value object's columns inside the {@code alerts} table.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AlertSourceEmbeddable {

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 40)
    private AlertSourceType sourceType;

    @Column(name = "source_reference_id", nullable = false)
    private UUID sourceReferenceId;
}
