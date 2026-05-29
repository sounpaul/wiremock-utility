package com.wiremock.utility.test.steps;

import com.wiremock.utility.test.client.BookingAPIClient;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.annotations.Steps;
import org.junit.Assert;

@Slf4j
public class BookingAPISteps {

    private Response response;
    private String bookingId;

    @Step
    public void invokeEmployeeService(String requestType) {
        BookingAPIClient bookingAPIClient = new BookingAPIClient();
        bookingAPIClient.initializeRequestSpecification();
        if (requestType.equals("get-booking")) {
            this.response = bookingAPIClient.getEmployeeById(bookingId);
        }
        log.info("Submitted request to {} endpoint", requestType);
    }

    @Step
    public void initBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    @Step
    public void validateResponseCode(int responseCode) {
        response.then().statusCode(responseCode);
    }

    @Step
    public void validateResponseBody(String responseBody) {
        JsonPath jsonPath = response.then().log().all().extract().jsonPath();
        Assert.assertEquals(responseBody, jsonPath.getString("msg"));
    }

}
