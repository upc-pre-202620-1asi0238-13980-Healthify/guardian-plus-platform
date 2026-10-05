package com.healthify.guardian.platform.healthmonitoring.interfaces.rest;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests of the configuration endpoints: vital sign types, wearable devices and thresholds (US12).
 */
class HealthMonitoringConfigurationControllersIntegrationTest extends HealthMonitoringRestTestBase {

    private final UUID recipient = UUID.randomUUID();

    @Test
    void registersAndListsVitalSignTypes() throws Exception {
        mockMvc.perform(post("/api/v1/vital-sign-types").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"glucose\",\"name\":\"Blood glucose\",\"unit\":\"mg/dL\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("GLUCOSE"));

        mockMvc.perform(post("/api/v1/vital-sign-types").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"GLUCOSE\",\"name\":\"Glucose\",\"unit\":\"mg/dL\"}"))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/v1/vital-sign-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void assignsListsAndDeactivatesAWearableDevice() throws Exception {
        var response = mockMvc.perform(post("/api/v1/wearable-devices").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"careRecipientProfileId\":\"%s\",\"serialNumber\":\"GP-0001\",\"deviceType\":\"WRISTBAND\"}"
                                .formatted(recipient)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ASSIGNED"))
                .andReturn().getResponse().getContentAsString();
        var deviceId = response.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(get("/api/v1/wearable-devices/care-recipient/{id}", recipient))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].serialNumber").value("GP-0001"));

        mockMvc.perform(delete("/api/v1/wearable-devices/{id}", deviceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));

        mockMvc.perform(delete("/api/v1/wearable-devices/{id}", deviceId))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void rejectsUnknownDeviceType() throws Exception {
        mockMvc.perform(post("/api/v1/wearable-devices").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"careRecipientProfileId\":\"%s\",\"serialNumber\":\"GP-0002\",\"deviceType\":\"PHONE\"}"
                                .formatted(recipient)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void definesAndDeactivatesAThreshold() throws Exception {
        var heartRate = context.registerType("HR", "Heart rate", "bpm");
        var body = "{\"careRecipientProfileId\":\"%s\",\"vitalSignTypeId\":\"%s\",\"minimumValue\":60,\"maximumValue\":100,\"requiredConsecutiveHits\":3}"
                .formatted(recipient, heartRate.getId().value());

        var response = mockMvc.perform(put("/api/v1/vital-sign-thresholds").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true))
                .andReturn().getResponse().getContentAsString();
        var thresholdId = response.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(get("/api/v1/vital-sign-thresholds/care-recipient/{id}", recipient))
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(delete("/api/v1/vital-sign-thresholds/{id}", thresholdId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(post("/api/v1/vital-sign-thresholds/{id}/activate", thresholdId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void rejectsThresholdWithZeroConsecutiveReadings() throws Exception {
        mockMvc.perform(put("/api/v1/vital-sign-thresholds").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"careRecipientProfileId\":\"%s\",\"vitalSignTypeId\":\"%s\",\"minimumValue\":60,\"maximumValue\":100,\"requiredConsecutiveHits\":0}"
                                .formatted(recipient, UUID.randomUUID())))
                .andExpect(status().isBadRequest());
    }
}
