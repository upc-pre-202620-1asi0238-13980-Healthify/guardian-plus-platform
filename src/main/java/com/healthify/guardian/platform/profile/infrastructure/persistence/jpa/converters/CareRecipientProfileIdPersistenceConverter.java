package com.healthify.guardian.platform.profile.infrastructure.persistence.jpa.converters;

import com.healthify.guardian.platform.profile.domain.model.valueobjects.CareRecipientProfileId;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.UUID;

/**
 * JPA converter between {@link CareRecipientProfileId} and UUID.
 */
@Converter
public class CareRecipientProfileIdPersistenceConverter
        implements AttributeConverter<CareRecipientProfileId, UUID> {

    @Override
    public UUID convertToDatabaseColumn(CareRecipientProfileId attribute) {
        return attribute != null ? attribute.value() : null;
    }

    @Override
    public CareRecipientProfileId convertToEntityAttribute(UUID dbData) {
        return dbData != null ? new CareRecipientProfileId(dbData) : null;
    }
}