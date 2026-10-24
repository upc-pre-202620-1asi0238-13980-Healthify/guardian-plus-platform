package com.healthify.guardian.platform.subscriptions.domain.model.entities;

import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.EntitlementCode;
import lombok.Getter;

@Getter
public class Entitlement {

    private EntitlementCode code;
    private String name;
    private String description;

    public Entitlement() {
    }

    public Entitlement(
            EntitlementCode code,
            String name,
            String description) {

        if (code == null) {
            throw new IllegalArgumentException(
                    "subscription.entitlement.code.invalid");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "subscription.entitlement.name.invalid");
        }

        this.code = code;
        this.name = name.strip();
        this.description =
                description == null
                        ? ""
                        : description.strip();
    }

    public boolean matches(EntitlementCode code) {
        return this.code.equals(code);
    }

    public void setCode(EntitlementCode code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}