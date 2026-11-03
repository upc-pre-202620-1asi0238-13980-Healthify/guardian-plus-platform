package com.healthify.guardian.platform.healthmonitoring.interfaces.rest;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests of {@code /api/v1/wearable-devices} (Link Wearable Device) and {@code /api/v1/vital-sign-types}.
 */
class WearableDevicesAndVitalSignTypesControllersIntegrationTest extends HealthMonitoringRestTestBase {

    private final UUID recipient = UUID.randomUUID();

    private static String device(UUID recipient, String serialNumber, String deviceType) {
        return "{\"careRecipientProfileId\":\"%s\",\"serialNumber\":\"%s\",\"deviceType\":\"%s\"}"
                .formatted(recipient, serialNumber, deviceType);
    }

    @Test
    void linksAndListsAWearableDevice() throws Exception {
        mockMvc.perform(post("/api/v1/wearable-devices").contentType(MediaType.APPLICATION_JSON)
                        .content(device(recipient, "GP-0001", "WRISTBAND")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.careRecipientProfileId").value(recipient.toString()))
                .andExpect(jsonPath("$.linkedAt").exists());

        mockMvc.perform(get("/api/v1/wearable-devices/care-recipient/{id}", recipient))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].serialNumber").value("GP-0001"));
    }

    @Test
    void listsEveryLinkedWearableDevice() throws Exception {
        mockMvc.perform(post("/api/v1/wearable-devices").contentType(MediaType.APPLICATION_JSON)
                .content(device(recipient, "GP-0001", "WRISTBAND")));
        mockMvc.perform(post("/api/v1/wearable-devices").contentType(MediaType.APPLICATION_JSON)
                .content(device(UUID.randomUUID(), "GP-0002", "SMARTWATCH")));

        mockMvc.perform(get("/api/v1/wearable-devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].careRecipientProfileId").exists());
    }

    @Test
    void aSerialNumberCanOnlyBeLinkedOnce() throws Exception {
        mockMvc.perform(post("/api/v1/wearable-devices").contentType(MediaType.APPLICATION_JSON)
                .content(device(recipient, "GP-0001", "WRISTBAND")));

        mockMvc.perform(post("/api/v1/wearable-devices").contentType(MediaType.APPLICATION_JSON)
                        .content(device(UUID.randomUUID(), "GP-0001", "SMARTWATCH")))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsUnknownDeviceType() throws Exception {
        mockMvc.perform(post("/api/v1/wearable-devices").contentType(MediaType.APPLICATION_JSON)
                        .content(device(recipient, "GP-0002", "PHONE")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listsEveryVitalSignTypeWithItsReferenceRanges() throws Exception {
        mockMvc.perform(get("/api/v1/vital-sign-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(6))
                .andExpect(jsonPath("$[0].code").value("HR"))
                .andExpect(jsonPath("$[0].unit").value("bpm"))
                .andExpect(jsonPath("$[0].normalMinimum").value(60))
                .andExpect(jsonPath("$[0].normalMaximum").value(100))
                .andExpect(jsonPath("$[0].physicalMinimum").value(20))
                .andExpect(jsonPath("$[0].physicalMaximum").value(250));
    }
}
