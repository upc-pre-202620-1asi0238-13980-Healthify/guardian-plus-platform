package com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects;

import java.util.UUID;

public record FragileCitizenId(UUID value) {
    public FragileCitizenId { if (value == null) throw new IllegalArgumentException("FragileCitizenId cannot be null."); }
}
