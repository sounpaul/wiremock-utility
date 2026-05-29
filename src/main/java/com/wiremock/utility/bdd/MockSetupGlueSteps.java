package com.wiremock.utility.bdd;

import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.wiremock.utility.MockServerFactory;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.wiremock.utility.MockFactory.createMock;

@Slf4j
public class MockSetupGlueSteps {

    private final List<StubMapping> scenarioStubs = new ArrayList<>();

    @Before
    public void beforeScenario() {
        scenarioStubs.clear();
    }

    @After
    public void afterScenario() {
        scenarioStubs.forEach(stub -> {
            MockServerFactory.wireMock().removeStubMapping(stub);
            log.debug("Removed stub mapping with id {}", stub.getId());
        });
        scenarioStubs.clear();
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
        List<Map<String, String>> valuesList = table.asMaps(String.class, String.class);
        createMockWithData(service, apiMethod, "default", valuesList);
    }

    @Given("{string} service mock for api method {string} using template {string} with data")
    public void given_mock_with_template_and_data(String service, String apiMethod, String template, DataTable table) {
        List<Map<String, String>> valuesList = table.asMaps(String.class, String.class);
        createMockWithData(service, apiMethod, template, valuesList);
    }

    private void createMockWithData(String service, String apiMethod, String template, List<Map<String, String>> valuesList) {
        valuesList.forEach(values -> scenarioStubs.add(createMock(service, apiMethod, template, values)));
    }

    private void createMockNoData(String service, String apiMethod, String template) {
        scenarioStubs.add(createMock(service, apiMethod, template, null));
    }
}
