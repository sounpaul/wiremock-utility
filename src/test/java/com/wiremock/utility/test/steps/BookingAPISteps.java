package com.wiremock.utility.test.steps;

import com.wiremock.utility.test.client.BookingAPIClient;
import com.wiremock.utility.test.model.BookingDates;
import com.wiremock.utility.test.model.BookingRequest;
import io.cucumber.datatable.DataTable;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.annotations.Step;
import org.junit.Assert;

import java.util.Map;

import static com.wiremock.utility.MockServerFactory.wireMock;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

@Slf4j
public class BookingAPISteps {

    private Response response;
    private String bookingId;
    private BookingRequest bookingRequest;

    @Step
    public void invokeEmployeeService(String requestType) {
        BookingAPIClient bookingAPIClient = new BookingAPIClient();
        bookingAPIClient.initializeRequestSpecification();
        if (requestType.equals("get-booking")) {
            this.response = bookingAPIClient.getEmployeeById(bookingId);
        } else if (requestType.equals("create-booking")) {
            this.response = bookingAPIClient.createBooking(bookingRequest);
        }
        log.info("Submitted request to {} endpoint", requestType);
    }

    @Step
    public void initBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    @Step
    public void validateResponseCode(int responseCode) {
        response.then().log().all().statusCode(responseCode);
    }

    @Step
    public void validateResponseBody(String responseBody) {
        JsonPath jsonPath = response.then().extract().jsonPath();
        Assert.assertEquals(responseBody, jsonPath.getString("msg"));
    }

    @Step
    public void initCreateBookingRequest(DataTable dataTable) {
        Map<String, String> bookingReqAsMap = dataTable.asMaps(String.class, String.class).getFirst();
        this.bookingRequest = BookingRequest.builder()
                .firstname(bookingReqAsMap.get("firstname"))
                .lastname(bookingReqAsMap.get("lastname"))
                .totalprice(Integer.parseInt(bookingReqAsMap.get("totalprice")))
                .depositpaid((bookingReqAsMap.get("depositpaid").equals("true")))
                .bookingdates(BookingDates.builder().checkin(bookingReqAsMap.get("checkin")).checkout(bookingReqAsMap.get("checkout")).build())
                .additionalneeds(bookingReqAsMap.get("additionalneeds"))
                .build();
    }

    @Step
    public void validateBookingPayload(int timesCalled, DataTable dataTable) {
        Map<String, String> bookingReqAsMap = dataTable.asMaps(String.class, String.class).getFirst();
        BookingRequest expectedBookingReqPayload = BookingRequest.builder()
                .firstname(bookingReqAsMap.get("firstname"))
                .lastname(bookingReqAsMap.get("lastname"))
                .totalprice(Integer.parseInt(bookingReqAsMap.get("totalprice")))
                .depositpaid((bookingReqAsMap.get("depositpaid").equals("true")))
                .bookingdates(BookingDates.builder().checkin(bookingReqAsMap.get("checkin")).checkout(bookingReqAsMap.get("checkout")).build())
                .additionalneeds(bookingReqAsMap.get("additionalneeds"))
                .build();

        wireMock()
                .verifyThat(timesCalled, postRequestedFor(urlPathEqualTo("/booking"))
                        .withRequestBody(matchingJsonPath("$.firstname", equalTo(expectedBookingReqPayload.getFirstname())))
                        .withRequestBody(matchingJsonPath("$.lastname", equalTo(expectedBookingReqPayload.getLastname())))
                        .withRequestBody(matchingJsonPath("$.totalprice", equalTo(String.valueOf(expectedBookingReqPayload.getTotalprice()))))
                        .withRequestBody(matchingJsonPath("$.depositpaid", equalTo(String.valueOf(expectedBookingReqPayload.isDepositpaid()))))
                        .withRequestBody(matchingJsonPath("$.bookingdates.checkin", equalTo(expectedBookingReqPayload.getBookingdates().getCheckin())))
                        .withRequestBody(matchingJsonPath("$.bookingdates.checkout", equalTo(expectedBookingReqPayload.getBookingdates().getCheckout())))
                        .withRequestBody(matchingJsonPath("$.additionalneeds", equalTo(expectedBookingReqPayload.getAdditionalneeds())))
                );
    }

}
