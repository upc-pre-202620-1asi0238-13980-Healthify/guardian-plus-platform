package com.healthify.guardian.platform.healthmonitoring.interfaces.rest;

import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSignType;
import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.WearableDevice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests of {@code /api/v1/vital-signs} (US01-US05, US21, TS02).
 */
class VitalSignsControllerIntegrationTest extends HealthMonitoringRestTestBase {

    private final UUID recipient = UUID.randomUUID();
    private VitalSignType heartRate;
    private WearableDevice device;

    @BeforeEach
    void setUp() {
        heartRate = context.registerType("HR", "Heart rate", "bpm");
        device = context.assignDevice(recipient, "GP-0001");
    }

    private String reading(String value, Instant measuredAt) {
        return """
                {"wearableDeviceId":"%s","careRecipientProfileId":"%s","vitalSignTypeId":"%s","value":%s,"measuredAt":"%s"}
                """.formatted(device.getId().value(), recipient, heartRate.getId().value(), value, measuredAt);
    }

    @Test
    void detectsAReadingAndEmitsIt() throws Exception {
        mockMvc.perform(post("/api/v1/vital-signs").contentType(MediaType.APPLICATION_JSON)
                        .content(reading("72", Instant.now().minusSeconds(5))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.careRecipientProfileId").value(recipient.toString()))
                .andExpect(jsonPath("$.value").value(72))
                .andExpect(jsonPath("$.emittedAt").exists());
    }

    @Test
    void rejectsReadingWithoutValue() throws Exception {
        mockMvc.perform(post("/api/v1/vital-signs").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"wearableDeviceId":"%s","careRecipientProfileId":"%s","vitalSignTypeId":"%s","measuredAt":"2026-10-05T10:00:00Z"}
                                """.formatted(device.getId().value(), recipient, heartRate.getId().value())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void rejectsReadingOfUnknownDevice() throws Exception {
        mockMvc.perform(post("/api/v1/vital-signs").contentType(MediaType.APPLICATION_JSON)
                        .content(reading("72", Instant.now()).replace(device.getId().value().toString(), UUID.randomUUID().toString())))
                .andExpect(status().isNotFound());
    }

    @Test
    void ingestsABatchSkippingDuplicates() throws Exception {
        var first = Instant.now().minusSeconds(120);
        var second = Instant.now().minusSeconds(60);
        var body = "{\"readings\":[%s,%s,%s]}".formatted(reading("70", first), reading("71", second), reading("70", first));

        mockMvc.perform(post("/api/v1/vital-signs/batches").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.received").value(3))
                .andExpect(jsonPath("$.stored").value(2))
                .andExpect(jsonPath("$.duplicatesSkipped").value(1));
    }

    @Test
    void rejectsBatchWithAnInvalidReadingAsUnprocessable() throws Exception {
        var invalid = reading("70", Instant.now()).replace(recipient.toString(), UUID.randomUUID().toString());
        var body = "{\"readings\":[%s,%s]}".formatted(reading("70", Instant.now().minusSeconds(10)), invalid);

        mockMvc.perform(post("/api/v1/vital-signs/batches").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void liveViewClassifiesTheLatestReading() throws Exception {
        context.defineThreshold(recipient, heartRate, "60", "100", 3);
        mockMvc.perform(post("/api/v1/vital-signs").contentType(MediaType.APPLICATION_JSON)
                .content(reading("112", Instant.now().minusSeconds(5))));

        mockMvc.perform(get("/api/v1/vital-signs/live/{id}", recipient))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vitalSigns[0].vitalSignTypeCode").value("HR"))
                .andExpect(jsonPath("$.vitalSigns[0].classification").value("ABOVE_RANGE"))
                .andExpect(jsonPath("$.vitalSigns[0].liveSignal").value(true));
    }

    @Test
    void historyRejectsInvertedPeriod() throws Exception {
        mockMvc.perform(get("/api/v1/vital-signs/history/{id}", recipient).param("from", "2026-10-05").param("to", "2026-10-01"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownVitalSignIsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/vital-signs/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("VITALSIGN_NOT_FOUND"));
    }
}
