package com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects;

public record WristbandCapabilityProfile(
        String wristbandModel,
        boolean hasCamera,
        boolean hasScreen,
        boolean hasMicrophone,
        boolean hasSpeaker,
        boolean supportsHapticAlerts,
        boolean supportsTelemetry,
        String citizenPhone,
        String caregiverPhone
) {
    public WristbandCapabilityProfile {
        if (wristbandModel == null || wristbandModel.isBlank()) {
            throw new IllegalArgumentException("Wristband model cannot be null or blank");
        }
    }

    public boolean supportsVideoCall() {
        return hasCamera && hasScreen && hasMicrophone && hasSpeaker;
    }

    public boolean supportsVoiceCall() {
        return hasMicrophone && hasSpeaker;
    }

    public static WristbandCapabilityProfile guardianPro(String citizenPhone, String caregiverPhone) {
        return new WristbandCapabilityProfile(
                "Guardian Pro",
                true,
                true,
                true,
                true,
                true,
                true,
                citizenPhone,
                caregiverPhone
        );
    }

    public static WristbandCapabilityProfile guardianBandLite(String citizenPhone, String caregiverPhone) {
        return new WristbandCapabilityProfile(
                "Guardian Band Lite",
                false,
                true,
                true,
                true,
                true,
                true,
                citizenPhone,
                caregiverPhone
        );
    }

    public static WristbandCapabilityProfile guardianCore(String citizenPhone, String caregiverPhone) {
        return new WristbandCapabilityProfile(
                "Guardian Core",
                false,
                false,
                false,
                false,
                true,
                true,
                citizenPhone,
                caregiverPhone
        );
    }
}
