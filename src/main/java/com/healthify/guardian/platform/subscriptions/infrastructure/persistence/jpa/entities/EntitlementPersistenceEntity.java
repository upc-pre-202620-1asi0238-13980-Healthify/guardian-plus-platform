package com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "entitlements")
public class EntitlementPersistenceEntity {

    @Id
    @Column(
            name = "code",
            nullable = false,
            updatable = false,
            length = 100
    )
    private String code;

    @Column(
            name = "name",
            nullable = false,
            length = 150
    )
    private String name;

    @Column(
            name = "description",
            length = 500
    )
    private String description;

    protected EntitlementPersistenceEntity() {
    }

    public EntitlementPersistenceEntity(
            String code,
            String name,
            String description
    ) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}