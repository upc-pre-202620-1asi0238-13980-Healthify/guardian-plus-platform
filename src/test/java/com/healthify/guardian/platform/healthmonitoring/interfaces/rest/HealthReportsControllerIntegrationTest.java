package com.healthify.guardian.platform.healthmonitoring.interfaces.rest;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests of {@code /api/v1/health-reports} (US07, US19).
 */
class HealthReportsControllerIntegrationTest extends HealthMonitoringRestTestBase {

    private final UUID recipient = UUID.randomUUID();

    private String request(LocalDate from, LocalDate to) {
        return "{\"careRecipientProfileId\":\"%s\",\"generatedByUserId\":\"%s\",\"periodStart\":\"%s\",\"periodEnd\":\"%s\"}"
                .formatted(recipient, UUID.randomUUID(), from, to);
    }

    @Test
    void generatesReportForPeriodWithReadings() throws Exception {
        var heartRate = context.registerType("HR", "Heart rate", "bpm");
        var device = context.assignDevice(recipient, "GP-0001");
        var today = LocalDate.now(ZoneOffset.UTC);
        context.detect(device, heartRate, "70", Instant.now().minusSeconds(60));
        context.detect(device, heartRate, "80", Instant.now().minusSeconds(30));

        var response = mockMvc.perform(post("/api/v1/health-reports").contentType(MediaType.APPLICATION_JSON)
                        .content(request(today.minusDays(6), today)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reportType").value("ON_DEMAND"))
                .andExpect(jsonPath("$.summaries[0].metricType").value("HR"))
                .andExpect(jsonPath("$.summaries[0].averageValue").value(75.0))
                .andReturn().getResponse().getContentAsString();
        var reportId = response.replaceAll("^\\{\"id\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(get("/api/v1/health-reports/{id}", reportId)).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/health-reports/care-recipient/{id}", recipient))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void emptyPeriodIsUnprocessable() throws Exception {
        mockMvc.perform(post("/api/v1/health-reports").contentType(MediaType.APPLICATION_JSON)
                        .content(request(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30))))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("BUSINESS_RULE_VIOLATION"));
    }

    @Test
    void unknownReportIsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/health-reports/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("HEALTHREPORT_NOT_FOUND"));
    }
}
