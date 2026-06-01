package com.wiremock.utility.test.client;

import com.wiremock.utility.test.model.BookingRequest;
import com.wiremock.utility.test.utils.TestConstants;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

import static net.serenitybdd.rest.SerenityRest.given;

public class BookingAPIClient {

    private RequestSpecification requestSpecification;

    public void initializeRequestSpecification() {
        this.requestSpecification = given().relaxedHTTPSValidation().log().all().baseUri(TestConstants.APP_BASE_URI);
    }


    public Response createBooking(BookingRequest bookingRequest) {
        return requestSpecification.headers(Map.of("Content-Type", "application/json")).body(bookingRequest).post(TestConstants.ADD_BOOKING_ENDPOINT);
    }

    public Response getEmployeeById(String bookingId) {
        return requestSpecification.headers(Map.of("Content-Type", "application/json")).pathParam("bookingId", bookingId).get(TestConstants.GET_BOOKING_ENDPOINT);
    }

}
