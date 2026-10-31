package com.healthify.guardian.platform.emergencyalerting.interfaces.rest.transform;

import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.RegisterDeliveryResultCommand;
import com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources.DeliveryStatusCallbackResource;

/**
 * Assembler to convert a {@link DeliveryStatusCallbackResource} to a {@link RegisterDeliveryResultCommand}.
 */
public final class RegisterDeliveryResultCommandFromResourceAssembler {

    private RegisterDeliveryResultCommandFromResourceAssembler() {
    }

    public static RegisterDeliveryResultCommand toCommandFromResource(DeliveryStatusCallbackResource resource) {
        return new RegisterDeliveryResultCommand(
                resource.alertId(), resource.deliveryId(), resource.status(), resource.occurredAt());
    }
}
