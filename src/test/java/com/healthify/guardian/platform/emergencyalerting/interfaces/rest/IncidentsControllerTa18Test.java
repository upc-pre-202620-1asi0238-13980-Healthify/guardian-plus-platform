package com.healthify.guardian.platform.emergencyalerting.interfaces.rest;

import com.healthify.guardian.platform.emergencyalerting.application.commandservices.IncidentCommandService;
import com.healthify.guardian.platform.emergencyalerting.application.queryservices.IncidentQueryService;
import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.Incident;
import com.healthify.guardian.platform.emergencyalerting.domain.model.queries.GetIncidentsQuery;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertSourceType;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.IncidentId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.IncidentStatus;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.Severity;
import com.healthify.guardian.platform.shared.interfaces.rest.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * TA-18 contract tests for GET /api/v1/incidents.
 *
 * These tests are intentionally isolated from the pre-existing incident
 * endpoints so the technical story can evolve without changing their
 * behaviour.
 */
class IncidentsControllerTa18Test {

    private final IncidentCommandService incidentCommandService = mock(IncidentCommandService.class);

    private final IncidentQueryService incidentQueryService = mock(IncidentQueryService.class);

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(
                    new IncidentsController(
                            incidentCommandService,
                            incidentQueryService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void listsIncidentsUsingTheRequestedFiltersAndPagination() throws Exception {
        var patientId = UUID.randomUUID();
        var incidentId = UUID.randomUUID();
        var alertId = UUID.randomUUID();

        var from = Instant.parse("2026-11-01T00:00:00Z");
        var to = Instant.parse("2026-11-20T23:59:59Z");

        var incident = incident(
                incidentId,
                alertId,
                Instant.parse("2026-11-15T12:00:00Z"));

        when(
                incidentQueryService.handle(
                        any(GetIncidentsQuery.class)))
                .thenReturn(
                        new PageImpl<>(
                                List.of(incident),
                                PageRequest.of(0, 20),
                                1));

        mockMvc.perform(
                get("/api/v1/incidents")
                        .param("patientId", patientId.toString())
                        .param("eventType", "FALL_DETECTED")
                        .param("severity", "CRITICAL")
                        .param("from", from.toString())
                        .param("to", to.toString())
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(
                        jsonPath("$.content[0].id")
                                .value(incidentId.toString()))
                .andExpect(
                        jsonPath("$.content[0].alertId")
                                .value(alertId.toString()))
                .andExpect(
                        jsonPath("$.content[0].status")
                                .value("IN_ATTENTION"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));

        var queryCaptor = ArgumentCaptor.forClass(GetIncidentsQuery.class);

        verify(incidentQueryService)
                .handle(queryCaptor.capture());

        var query = queryCaptor.getValue();

        assertNotNull(query.patientId());
        assertEquals(patientId, query.patientId().value());

        assertEquals(
                AlertSourceType.FALL_DETECTED,
                query.eventType());

        assertEquals(
                Severity.CRITICAL,
                query.severity());

        assertNotNull(query.dateRange());
        assertEquals(from, query.dateRange().from());
        assertEquals(to, query.dateRange().to());

        assertEquals(0, query.page());
        assertEquals(20, query.size());
    }

    @Test
    void criticalSeverityWithoutMatchesReturnsAnEmptyPage() throws Exception {
        var patientId = UUID.randomUUID();

        when(
                incidentQueryService.handle(
                        any(GetIncidentsQuery.class)))
                .thenReturn(
                        new PageImpl<>(
                                List.of(),
                                PageRequest.of(0, 20),
                                0));

        mockMvc.perform(
                get("/api/v1/incidents")
                        .param(
                                "patientId",
                                patientId.toString())
                        .param(
                                "severity",
                                "CRITICAL")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));

        var queryCaptor = ArgumentCaptor.forClass(GetIncidentsQuery.class);

        verify(incidentQueryService)
                .handle(queryCaptor.capture());

        var query = queryCaptor.getValue();

        assertNotNull(query.patientId());
        assertEquals(patientId, query.patientId().value());
        assertEquals(Severity.CRITICAL, query.severity());
        assertEquals(0, query.page());
        assertEquals(20, query.size());
    }

    private static Incident incident(
            UUID incidentId,
            UUID alertId,
            Instant markedInAttentionAt) {
        var incident = new Incident();

        incident.setId(
                new IncidentId(incidentId));

        incident.setAlertId(
                new AlertId(alertId));

        incident.setStatus(
                IncidentStatus.IN_ATTENTION);

        incident.setMarkedInAttentionAt(
                markedInAttentionAt);

        return incident;
    }
}