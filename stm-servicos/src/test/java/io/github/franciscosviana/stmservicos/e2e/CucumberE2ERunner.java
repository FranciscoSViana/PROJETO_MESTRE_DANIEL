package io.github.franciscosviana.stmservicos.e2e;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;
import org.junit.jupiter.api.Tag;

import static io.cucumber.junit.platform.engine.Constants.FEATURES_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

@Suite
@Tag("e2e")
@IncludeEngines("cucumber")
@ConfigurationParameter(
    key = GLUE_PROPERTY_NAME,
    value = "io.github.franciscosviana.stmservicos.e2e"
)
@ConfigurationParameter(
    key = FEATURES_PROPERTY_NAME,
    value = "src/test/resources/features"
)
@ConfigurationParameter(
    key = PLUGIN_PROPERTY_NAME,
    value = "pretty, html:build/reports/cucumber/e2e-report.html, json:build/reports/cucumber/e2e-report.json"
)
public class CucumberE2ERunner {
}
