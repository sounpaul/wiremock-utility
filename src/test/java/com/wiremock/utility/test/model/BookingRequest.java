package com.wiremock.utility.test.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequest {

    @JsonProperty("firstname")
    private String firstname;

    @JsonProperty("lastname")
    private String lastname;

    @JsonProperty("totalprice")
    private int totalprice;

    @JsonProperty("depositpaid")
    private boolean depositpaid;

    @JsonProperty("bookingdates")
    private BookingDates bookingdates;

    @JsonProperty("additionalneeds")
    private String additionalneeds;
}
