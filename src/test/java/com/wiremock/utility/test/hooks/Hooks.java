package com.wiremock.utility.test.hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.model.environment.EnvironmentSpecificConfiguration;
import net.thucydides.model.environment.SystemEnvironmentVariables;
import net.thucydides.model.util.EnvironmentVariables;

@Slf4j
public class Hooks {

    @Before(order = 1)
    public void before(Scenario scenario) {
        log.info("Start of scenario : {}", scenario.getName());
        EnvironmentVariables environmentVariables = SystemEnvironmentVariables.createEnvironmentVariables();
        EnvironmentSpecificConfiguration environmentSpecificConfiguration = EnvironmentSpecificConfiguration.from(environmentVariables);
        System.setProperty("wiremock_host", environmentSpecificConfiguration.getProperty("wiremock_host"));
        System.setProperty("wiremock_port", environmentSpecificConfiguration.getProperty("wiremock_port"));
        System.setProperty("wiremock_protocol", environmentSpecificConfiguration.getProperty("wiremock_protocol"));
        System.setProperty("app.base.url", environmentSpecificConfiguration.getProperty("app.baseURL"));
    }

    @After(order = 10)
    public void after(Scenario scenario) {
        log.info("End of scenario : {}", scenario.getName());
    }

}
