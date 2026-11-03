package com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.converters;

import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.UserId;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.UUID;

/**
 * Converts {@link UserId} to/from a {@code UUID} column for JPA persistence.
 * Shared by every persistence entity in this bounded context that references a Care Circle member.
 */
@Converter(autoApply = false)
public class UserIdPersistenceConverter implements AttributeConverter<UserId, UUID> {

    @Override
    public UUID convertToDatabaseColumn(UserId attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public UserId convertToEntityAttribute(UUID dbData) {
        return dbData == null ? null : new UserId(dbData);
    }
}
