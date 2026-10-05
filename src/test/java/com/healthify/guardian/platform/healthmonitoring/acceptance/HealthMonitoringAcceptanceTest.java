package com.healthify.guardian.platform.healthmonitoring.acceptance;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

/**
 * Runs the Health Monitoring BDD acceptance tests: the Gherkin features under
 * {@code src/test/resources/features/healthmonitoring} (US01-US05, US09, US19, US21, US24, TS02).
 * An HTML report is written to {@code target/cucumber-reports/health-monitoring.html}.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/healthmonitoring")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.healthify.guardian.platform.healthmonitoring.acceptance.steps")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty, html:target/cucumber-reports/health-monitoring.html")
public class HealthMonitoringAcceptanceTest {
}
