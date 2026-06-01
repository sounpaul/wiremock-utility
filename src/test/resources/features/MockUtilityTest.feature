Feature: Mock utility test feature

  Scenario Outline: Validate GET booking API when downstream returns 400 & 500
    Given "booking-svc" service mock for api method "get-booking" using template "<template>"
    When booking details will be fetched for "<bookingId>"
    When "get-booking" endpoint is invoked
    Then the response code should be "<responseCode>"
    And the response body should contain message "<responseBody>"

    Examples:
      | template              | responseCode | responseBody          | bookingId |
      | bad-request           | 400          | bad-request           | 1         |
      | internal-server-error | 500          | internal-server-error | 2         |


  Scenario Outline: Validate GET booking API when downstream returns 401
    Given "booking-svc" service mock for api method "get-booking"
    When booking details will be fetched for "<bookingId>"
    When "get-booking" endpoint is invoked
    Then the response code should be "<responseCode>"
    And the response body should contain message "<responseBody>"

    Examples:
      | responseCode | responseBody        | bookingId |
      | 401          | authorization-error | 3         |

  Scenario Outline: Validate GET booking API when downstream returns 400 & 500 while setting up mock with data
    Given "booking-svc" service mock for api method "get-booking" using template "<template>" with data
      | msg   |
      | <msg> |
    When booking details will be fetched for "<bookingId>"
    When "get-booking" endpoint is invoked
    Then the response code should be "<responseCode>"
    And the response body should contain message "<responseBody>"

    Examples:
      | template              | responseCode | responseBody              | bookingId | msg                       |
      | bad-request           | 400          | bad-request-new           | 4         | bad-request-new           |
      | internal-server-error | 500          | internal-server-error-new | 5         | internal-server-error-new |

  Scenario Outline: Validate GET booking API when downstream returns 401 while setting up mock with data
    Given "booking-svc" service mock for api method "get-booking" with data
      | msg   |
      | <msg> |
    When booking details will be fetched for "<bookingId>"
    When "get-booking" endpoint is invoked
    Then the response code should be "<responseCode>"
    And the response body should contain message "<responseBody>"

    Examples:
      | responseCode | responseBody            | bookingId | msg                     |
      | 401          | authorization-error-new | 6         | authorization-error-new |

  @CreateBooking
  Scenario Outline: Validate CREATE booking API payload when downstream returns 200
    Given "booking-svc" service mock for api method "create-booking"
    When the create-booking request contains below payload
      | firstname | lastname | totalprice | depositpaid | checkin    | checkout   | additionalneeds |
      | John      | Smith    | 200        | true        | 2026-06-01 | 2026-06-05 | breakfast       |
    When "create-booking" endpoint is invoked
    Then the response code should be "<responseCode>"
    And validate "create-booking" endpoint of booking-svc-api is called 1 times
      | firstname | lastname | totalprice | depositpaid | checkin    | checkout   | additionalneeds |
      | John      | Smith    | 200        | true        | 2026-06-01 | 2026-06-05 | breakfast       |

    Examples:
      | responseCode |
      | 200          |