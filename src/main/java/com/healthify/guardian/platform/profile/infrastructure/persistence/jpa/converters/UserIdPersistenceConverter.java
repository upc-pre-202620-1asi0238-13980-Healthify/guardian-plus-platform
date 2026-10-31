package com.healthify.guardian.platform.profile.infrastructure.persistence.jpa.converters;

import com.healthify.guardian.platform.profile.domain.model.valueobjects.UserId;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.UUID;

/**
 * JPA converter between {@link UserId} and its UUID representation.
 */
@Converter
public class UserIdPersistenceConverter implements AttributeConverter<UserId, UUID> {

    @Override
    public UUID convertToDatabaseColumn(UserId attribute) {
        return attribute != null ? attribute.value() : null;
    }

    @Override
    public UserId convertToEntityAttribute(UUID dbData) {
        return dbData != null ? new UserId(dbData) : null;
    }
}