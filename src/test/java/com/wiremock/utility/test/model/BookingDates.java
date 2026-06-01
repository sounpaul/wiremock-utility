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
public class BookingDates {

    @JsonProperty("checkin")
    private String checkin;

    @JsonProperty("checkout")
    private String checkout;
}
