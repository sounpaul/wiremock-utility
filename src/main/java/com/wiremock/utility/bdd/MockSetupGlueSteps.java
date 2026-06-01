package com.wiremock.utility.bdd;

import com.wiremock.utility.MockServerFactory;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static com.wiremock.utility.MockFactory.createMock;

@Slf4j
public class MockSetupGlueSteps {

    @Before
    public void beforeScenario() {
        MockServerFactory.wireMock().resetMappings();
    }

    @Given("{string} service mock for api method {string}")
    public void given_mock_with_default_template_and_no_data(String service, String apiMethod) {
        createMockNoData(service, apiMethod, "default");
    }

    @Given("{string} service mock for api method {string} using template {string}")
    public void given_mock_with_template_and_no_data(String service, String apiMethod, String template) {
        createMockNoData(service, apiMethod, template);
    }

    @Given("{string} service mock for api method {string} with data")
    public void given_mock_with_default_template_and_data(String service, String apiMethod, DataTable table) {
        Map<String, String> values = table.asMaps(String.class, String.class).getFirst();
        createMockWithData(service, apiMethod, "default", values);
    }

    @Given("{string} service mock for api method {string} using template {string} with data")
    public void given_mock_with_template_and_data(String service, String apiMethod, String template, DataTable table) {
        Map<String, String> values = table.asMaps(String.class, String.class).getFirst();
        createMockWithData(service, apiMethod, template, values);
    }

    private void createMockWithData(String service, String apiMethod, String template, Map<String, String> values) {
        createMock(service, apiMethod, template, values);
    }

    private void createMockNoData(String service, String apiMethod, String template) {
        createMock(service, apiMethod, template, null);
    }
}
