package com.healthify.guardian.platform.emergencyalerting.infrastructure.notifications;

import com.healthify.guardian.platform.emergencyalerting.application.outboundservices.AlertNotification;
import com.healthify.guardian.platform.emergencyalerting.application.outboundservices.NotificationPurpose;

/**
 * Title and body of the push notifications, in Spanish like the mobile app. They follow the alert titles the app
 * shows, so the notification and the screen it opens say the same thing.
 */
public final class PushNotificationTexts {

    /** What the phone shows in the notification shade. */
    public record PushContent(String title, String body) {
    }

    private PushNotificationTexts() {
    }

    public static PushContent of(AlertNotification notification) {
        var name = notification.careRecipientName() == null || notification.careRecipientName().isBlank()
                ? "la persona bajo cuidado"
                : notification.careRecipientName();
        if (notification.purpose() == NotificationPurpose.RESPONSE_CLAIMED) {
            return new PushContent("La alerta ya está siendo atendida",
                    "Otro integrante del Care Circle se hizo cargo de la alerta de " + name + ".");
        }
        return switch (notification.sourceType()) {
            case FALL_DETECTED -> new PushContent("Posible caída detectada",
                    "La pulsera de " + name + " detectó un impacto. Abre la alerta para responder.");
            case SOS_TRIGGERED -> new PushContent(name + " presionó el botón SOS",
                    "Pidió ayuda desde su pulsera. Abre la alerta para responder.");
            case VITAL_SIGN_ANOMALY -> new PushContent("Signo vital fuera de rango",
                    name + " tiene lecturas fuera de su rango seguro.");
            case SAFE_ZONE_VIOLATION -> new PushContent("Salida de zona segura",
                    name + " salió de su zona segura. Revisa su ubicación.");
            case PROLONGED_INACTIVITY -> new PushContent("Inactividad prolongada",
                    name + " lleva mucho tiempo sin moverse.");
            case REMINDER_REISSUED -> new PushContent("Recordatorio sin confirmar",
                    name + " no confirmó un recordatorio.");
            case MEDICATION_RESTOCK_SUGGESTED -> new PushContent("Medicamento por agotarse",
                    "Quedan pocos días de un medicamento de " + name + ".");
        };
    }
}
