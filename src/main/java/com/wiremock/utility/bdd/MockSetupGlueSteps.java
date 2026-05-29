package com.wiremock.utility.bdd;

import com.wiremock.utility.MockFactory;
import com.wiremock.utility.MockServerFactory;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;

import java.util.List;
import java.util.Map;

import static com.wiremock.utility.MockFactory.createMock;

public class MockSetupGlueSteps {

    @Before
    public void stopMock() {
        MockServerFactory.wireMock().resetMappings();
    }

    @Given("{string} service mock for api method {string}")
    public void given_mock_with_default_template_and_no_data(String service, String apiMethod) {
        createMockNoData(service, apiMethod, "default");
    }

    @Given("{string} service mock for api method {string} using template {string}")
    public void given_mock_with_template_and_no_data(
            String service, String apiMethod, String template) {
        createMockNoData(service, apiMethod, template);
    }

    @Given("{string} service mock for api method {string} with data")
    public void given_mock_with_default_template_and_data(
            String service, String apiMethod, List<Map<String, String>> valuesList) {
        createMockWithData(service, apiMethod, "default", valuesList);
    }

    @Given("{string} service mock for api method {string} using template {string} with data")
    public void given_mock_with_template_and_data(
            String service, String apiMethod, String template, List<Map<String, String>> valuesList) {
        createMockWithData(service, apiMethod, template, valuesList);
    }

    private void createMockWithData(
            String service,
            String apiMethod,
            String template,
            List<Map<String, String>> valuesList) {
        valuesList.forEach(values -> createMock(service, apiMethod, template, values));
    }

    private void createMockNoData(String service, String apiMethod, String template) {
        createMock(service, apiMethod, template, null);
    }
}
