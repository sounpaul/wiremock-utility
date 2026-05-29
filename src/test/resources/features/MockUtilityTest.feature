Feature: Mock utility test feature

  Scenario Outline: Validate GET booking API
    Given "booking-svc" service mock for api method "get-booking" using template "<template>"
    When booking details will be fetched for "<bookingId>"
    When "get-booking" endpoint is invoked
    Then the response code should be "<responseCode>"
    And the response body should contain message "<responseBody>"

    Examples:
      | template              | responseCode | responseBody              | bookingId |
      | bad-request           | 400          | bad-request               | 1         |
      | internal-server-error | 500          | internal-server-error     | 2         |