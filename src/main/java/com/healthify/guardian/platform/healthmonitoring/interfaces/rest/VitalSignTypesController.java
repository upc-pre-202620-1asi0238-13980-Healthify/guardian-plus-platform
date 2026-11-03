package com.healthify.guardian.platform.healthmonitoring.interfaces.rest;

import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignType;
import com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources.VitalSignTypeResource;
import com.healthify.guardian.platform.healthmonitoring.interfaces.rest.transform.VitalSignTypeResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * REST controller exposing the vital sign types monitored by the platform.
 */
@RestController
@RequestMapping(value = "/api/v1/vital-sign-types", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Vital Sign Types", description = "Monitored vital sign types and their reference ranges")
public class VitalSignTypesController {

    @GetMapping
    @Operation(summary = "List vital sign types",
            description = "Lists the vital sign types monitored by the wearable with their normal range and physical limits.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vital sign types retrieved",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = VitalSignTypeResource.class))))
    })
    public ResponseEntity<List<VitalSignTypeResource>> getAllVitalSignTypes() {
        return ResponseEntity.ok(Arrays.stream(VitalSignType.values())
                .map(VitalSignTypeResourceFromEntityAssembler::toResourceFromEntity)
                .toList());
    }
}
