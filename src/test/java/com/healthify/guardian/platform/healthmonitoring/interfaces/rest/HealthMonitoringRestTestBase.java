package com.healthify.guardian.platform.healthmonitoring.interfaces.rest;

import com.healthify.guardian.platform.healthmonitoring.application.internal.queryservices.HealthReportQueryServiceImpl;
import com.healthify.guardian.platform.healthmonitoring.application.internal.queryservices.WearableDeviceQueryServiceImpl;
import com.healthify.guardian.platform.healthmonitoring.testsupport.HealthMonitoringTestContext;
import com.healthify.guardian.platform.shared.interfaces.rest.GlobalExceptionHandler;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Integration test base: the real REST controllers of Health Monitoring wired to the real
 * application services over in-memory repositories, exercised through MockMvc with the
 * platform's global error handling. No database or Spring context is required.
 */
abstract class HealthMonitoringRestTestBase {

    protected final HealthMonitoringTestContext context = new HealthMonitoringTestContext();

    protected final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
                    new VitalSignsController(context.vitalSignCommandService, context.vitalSignQueryService, 60),
                    new WearableDevicesController(context.wearableDeviceCommandService,
                            new WearableDeviceQueryServiceImpl(context.wearableDeviceRepository)),
                    new VitalSignTypesController(),
                    new HealthReportsController(context.healthReportCommandService,
                            new HealthReportQueryServiceImpl(context.healthReportRepository)))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
}
