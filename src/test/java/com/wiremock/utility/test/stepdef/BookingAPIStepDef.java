package com.wiremock.utility.test.stepdef;

import com.wiremock.utility.test.steps.BookingAPISteps;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.*;
import net.serenitybdd.annotations.Steps;

public class BookingAPIStepDef {

    @Steps
    BookingAPISteps bookingAPISteps;

    @When("booking details will be fetched for {string}")
    public void booking_details_will_be_fetched_for(String bookingId) {
        bookingAPISteps.initBookingId(bookingId);
    }

    @When("{string} endpoint is invoked")
    public void endpoint_is_invoked(String endpointName) {
        bookingAPISteps.invokeEmployeeService(endpointName);
    }

    @Then("the response code should be {string}")
    public void the_response_code_should_be(String responseCode) {
        bookingAPISteps.validateResponseCode(Integer.parseInt(responseCode));
    }

    @Then("the response body should contain message {string}")
    public void the_response_body_should_contain_message(String responseBody) {
        bookingAPISteps.validateResponseBody(responseBody);
    }

    @And("validate {string} endpoint of booking-svc-api is called {int} times")
    public void booking_svc_api_payload_called_validation(String endpointName, int timesCalled, DataTable dataTable) {
        bookingAPISteps.validateBookingPayload(timesCalled, dataTable);
    }

    @When("the create-booking request contains below payload")
    public void booking_api_init_payload(DataTable dataTable) {
        bookingAPISteps.initCreateBookingRequest(dataTable);
    }

}
