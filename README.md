# WireMock Utility

A shared Java library that simplifies setting up and managing [WireMock](https://wiremock.org/) stub servers in automated test suites. It provides a file-driven templating system for defining mock responses, a singleton server factory, and ready-to-use Cucumber BDD step definitions powered by [Serenity BDD](https://serenity-bdd.info/).

---

## Table of Contents

- [Requirements](#requirements)
- [Build](#build)
- [Project Structure](#project-structure)
- [Architecture Overview](#architecture-overview)
- [Classes](#classes)
  - [MockServerFactory](#mockserverfactory)
  - [MockFactory](#mockfactory)
  - [MockSetupGlueSteps](#mocksetupgluesteps)
- [Resource File Structure](#resource-file-structure)
  - [mock.json](#mockjson)
  - [body.json](#bodyjson)
  - [default.properties](#defaultproperties)
  - [Template Tokens](#template-tokens)
  - [Translate Special Values](#translate-special-values)
- [System Properties](#system-properties)
- [BDD Tests](#bdd-tests)
  - [Test Structure](#test-structure)
  - [Runner — BookingAPIRunner](#runner--bookingapirunner)
  - [Feature Files](#feature-files)
  - [Step Definitions](#step-definitions)
  - [Serenity Configuration](#serenity-configuration)
  - [Logging Configuration](#logging-configuration)
  - [Running BDD Tests](#running-bdd-tests)
  - [Serenity Report](#serenity-report)
- [BDD Usage in Consumer Projects](#bdd-usage-in-consumer-projects)
  - [Glue Path Setup](#glue-path-setup)
  - [Writing Feature Steps](#writing-feature-steps)
  - [Stub Lifecycle](#stub-lifecycle)
- [Programmatic Usage](#programmatic-usage)
- [Logging](#logging)
- [Dependencies](#dependencies)

---

## Requirements

| Tool | Version |
|------|---------|
| Java | 21+ |
| Gradle (via wrapper) | 8.8 |

No global Gradle installation is needed — use the provided `gradlew` / `gradlew.bat` wrapper scripts.

---

## Build

### Compile only

```bash
# Windows
.\gradlew.bat compileJava

# Linux / macOS
./gradlew compileJava
```

### Build the fat JAR (all runtime dependencies bundled)

```bash
.\gradlew.bat build
```

> **Note:** The `build` task **skips tests** by design. Tests must be run explicitly (see [Running BDD Tests](#running-bdd-tests)). The fat JAR is produced at:
> ```
> build/libs/wiremock-utility-1.0.0-all.jar
> ```

### Publish to local Maven repository

```bash
.\gradlew.bat publishToMavenLocal
```

---

## Project Structure

```
wiremock-utility/
├── src/
│   ├── main/
│   │   ├── java/com/wiremock/utility/
│   │   │   ├── MockServerFactory.java        ← WireMock client/server singleton
│   │   │   ├── MockFactory.java              ← Template loader & stub registrar
│   │   │   └── bdd/
│   │   │       └── MockSetupGlueSteps.java   ← Cucumber @Given / @Before / @After
│   │   └── resources/
│   │       └── logback.xml                   ← Main logging config
│   └── test/
│       ├── java/com/wiremock/utility/test/
│       │   ├── runner/
│       │   │   └── BookingAPIRunner.java      ← Cucumber + Serenity test runner
│       │   ├── steps/                         ← Project-specific step definitions
│       │   └── hooks/                         ← Project-specific Cucumber hooks
│       └── resources/
│           ├── features/
│           │   └── MockUtilityTest.feature    ← BDD feature file
│           ├── mocks/
│           │   └── booking-svc/
│           │       └── get-booking/
│           │           ├── success/           ← mock.json, body.json, default.properties
│           │           ├── bad-request/
│           │           └── internal-server-error/
│           ├── junit-platform.properties      ← Cucumber parallel / execution config
│           ├── serenity.conf                  ← Serenity environment config
│           └── logback-test.xml              ← Test logging config
├── build.gradle
├── settings.gradle
└── gradlew / gradlew.bat
```

---

## Architecture Overview

```
Feature file (.feature)
        │
        │  @Given / @When / @Then
        ▼
BookingAPIRunner               ← JUnit 5 @Suite — discovers & runs all .feature files
        │
        ├─── MockSetupGlueSteps  (@Before / @After / @Given)
        │         │
        │         │  createMock(service, apiMethod, template, values)
        │         ▼
        │    MockFactory         ← Loads & renders template files from classpath
        │         │  reads: mock.json, body.json, default.properties
        │         │  applies token replacement + value translation
        │         │
        │         │  register(StubMapping)
        │         ▼
        │    MockServerFactory   ← Singleton WireMock client / server manager
        │         │
        │         ▼
        │    WireMock server     ← Local (auto-started on port 9081) or remote
        │
        └─── Project step defs  (@When / @Then — HTTP calls, assertions, etc.)
                  │
                  │  uses baseUrl from MockServerFactory.getBaseUrl()
                  ▼
             Service under test
```

---

## Classes

### MockServerFactory

**Package:** `com.wiremock.utility`

A thread-safe singleton that manages the WireMock client connection. On the first call to `wireMock()` it reads system properties to decide whether to connect to a local or remote WireMock instance.

```java
// Get the WireMock client (initialises on first call)
WireMock client = MockServerFactory.wireMock();

// Get the base URL of the mock server (e.g. "http://localhost:9081")
String baseUrl = MockServerFactory.getBaseUrl();

// Get the WireMockServer instance (only non-null when running locally)
WireMockServer server = MockServerFactory.getWireMockServer();
```

**Behaviour:**

- Reads `wiremock_host` system property (default: `localhost`).
- If host is `localhost`: creates and starts a `WireMockServer` on port **9081**. If the server is already running (`FatalStartupException`), it logs a warning and continues.
- If host is anything else: connects to the remote WireMock server using `wiremock_protocol`, `wiremock_host`, and `wiremock_port` — no local server is started.

---

### MockFactory

**Package:** `com.wiremock.utility`

Loads stub definitions from classpath resources, performs token substitution, and registers them with the WireMock client. Returns the registered `StubMapping` so callers can track and clean up individual stubs.

```java
// Register a stub with no dynamic data (uses default.properties only)
StubMapping stub = MockFactory.createMock("booking-svc", "get-booking", "default", null);

// Register a stub with runtime overrides
Map<String, String> overrides = Map.of("msg", "custom-message");
StubMapping stub = MockFactory.createMock("booking-svc", "get-booking", "success", overrides);
```

**How it works:**

1. Builds the resource path: `mocks/{service}/{apiMethod}/{template}/`
2. Reads `body.json` and JSON-encodes it (escaping quotes/special characters).
3. Reads `default.properties` and merges any runtime `values` map on top.
4. Reads `mock.json` and replaces `[[body]]` with the encoded body string.
5. Replaces every `[[propertyName]]` token with the value from merged properties, passing each value through `translate()`.
6. Registers the stub via `MockServerFactory.wireMock().register(StubMapping.buildFrom(...))` and returns the `StubMapping`.

---

### MockSetupGlueSteps

**Package:** `com.wiremock.utility.bdd`

Cucumber step definitions that delegate to `MockFactory.createMock()`. Each registered stub is tracked internally and removed individually after each scenario.

| Annotation | Step Pattern |
|-----------|-------------|
| `@Before` | *(runs before every scenario)* — clears the internal stub tracking list |
| `@After` | *(runs after every scenario)* — removes only the stubs registered during that scenario |
| `@Given` | `"{string} service mock for api method {string}"` |
| `@Given` | `"{string} service mock for api method {string} using template {string}"` |
| `@Given` | `"{string} service mock for api method {string} with data"` |
| `@Given` | `"{string} service mock for api method {string} using template {string} with data"` |

---

## Resource File Structure

Mock template files must be placed under `src/test/resources/mocks/` following this layout:

```
src/test/resources/
└── mocks/
    └── {service}/
        └── {apiMethod}/
            └── {template}/
                ├── mock.json           ← WireMock stub mapping
                ├── body.json           ← Raw response body
                └── default.properties  ← Default token values
```

**Example** — `booking-svc` / `get-booking` with three templates:

```
mocks/
└── booking-svc/
    └── get-booking/
        ├── success/
        │   ├── mock.json
        │   ├── body.json
        │   └── default.properties   (msg=data-fetched-successfully)
        ├── bad-request/
        │   ├── mock.json
        │   ├── body.json
        │   └── default.properties   (msg=bad-request)
        └── internal-server-error/
            ├── mock.json
            ├── body.json
            └── default.properties   (msg=internal-server-error)
```

---

### mock.json

A standard WireMock stub mapping. Use `[[body]]` where the response body should be injected and `[[tokenName]]` for any other dynamic values.

```json
{
  "request": {
    "method": "GET",
    "urlPathPattern": "/booking/[^/]+"
  },
  "response": {
    "status": 200,
    "headers": {
      "Content-Type": "application/json"
    },
    "body": "[[body]]"
  }
}
```

The `status` field in each template drives the HTTP response code — `200` for `success`, `400` for `bad-request`, `500` for `internal-server-error`.

---

### body.json

The raw (unescaped) JSON response body. Write it as plain JSON — `MockFactory` handles the escaping automatically before injecting it into `mock.json`.

```json
{
  "msg": "[[msg]]"
}
```

---

### default.properties

Key-value pairs providing default values for all `[[tokens]]` used in `mock.json` and `body.json`. Values can be overridden at runtime by passing a `Map<String, String>` to `createMock()`.

```properties
# success/default.properties
msg=data-fetched-successfully

# bad-request/default.properties
msg=bad-request

# internal-server-error/default.properties
msg=internal-server-error
```

---

### Template Tokens

Tokens follow the pattern `[[tokenName]]` in both `mock.json` and `body.json`. `MockFactory` replaces every token whose name matches a key in the merged properties.

| Token in file | Key in properties | Resolved value |
|---------------|-------------------|----------------|
| `[[msg]]` | `msg` | value from `default.properties` / override map |
| `[[body]]` | *(reserved)* | rendered + escaped content of `body.json` |

---

### Translate Special Values

Property values pass through a `translate()` function before substitution, supporting human-readable keywords for edge-case values:

| Value in properties / data table | Substituted with |
|----------------------------------|-----------------|
| `empty` | `""` (empty string) |
| `empty string` | `""` (empty string) |
| `space` | `" "` (single space) |
| `N space` or `N spaces` (e.g. `3 spaces`) | N space characters |
| *(anything else)* | the value as-is |

---

## System Properties

| Property | Default | Description |
|----------|---------|-------------|
| `wiremock_host` | `localhost` | WireMock server hostname. Set to a remote host to skip local server startup. |
| `wiremock_port` | `9081` | WireMock server port. |
| `wiremock_protocol` | `http` | Protocol (`http` or `https`). |

These are also configurable per environment in `serenity.conf` (see [Serenity Configuration](#serenity-configuration)).

---

## BDD Tests

### Test Structure

```
src/test/
├── java/com/wiremock/utility/test/
│   ├── runner/
│   │   └── BookingAPIRunner.java    ← Entry point for gradle test
│   ├── steps/                       ← @When / @Then step definitions
│   └── hooks/                       ← Additional @Before / @After hooks
└── resources/
    ├── features/
    │   └── MockUtilityTest.feature
    ├── mocks/                        ← WireMock template files
    ├── junit-platform.properties
    ├── serenity.conf
    └── logback-test.xml
```

---

### Runner — BookingAPIRunner

**File:** `src/test/java/com/wiremock/utility/test/runner/BookingAPIRunner.java`

```java
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(
    key = GLUE_PROPERTY_NAME,
    value = "com.wiremock.utility.bdd, com.wiremock.utility.test.stepdef, com.wiremock.utility.test.hooks"
)
@ConfigurationParameter(
    key = PLUGIN_PROPERTY_NAME,
    value = "io.cucumber.core.plugin.SerenityReporterParallel, pretty, timeline:build/test-results/timeline"
)
public class BookingAPIRunner {}
```

| Annotation | Purpose |
|-----------|---------|
| `@Suite` | Marks this as a JUnit Platform suite — discovered by `junit-platform-suite-engine` |
| `@IncludeEngines("cucumber")` | Delegates test discovery and execution to the Cucumber engine |
| `@SelectClasspathResource("features")` | Scans `src/test/resources/features/` for all `.feature` files |
| `GLUE_PROPERTY_NAME` | Comma-separated packages where Cucumber looks for step definitions and hooks. **All packages must be in a single annotation** — duplicate keys are silently dropped by Cucumber |
| `PLUGIN_PROPERTY_NAME` | `SerenityReporterParallel` hooks into the test lifecycle to generate the Serenity HTML report; `pretty` prints step output to console; `timeline` writes execution timeline to `build/test-results/timeline` |

---

### Feature Files

**File:** `src/test/resources/features/MockUtilityTest.feature`

```gherkin
Feature: Mock utility test feature

  Scenario Outline: Validate GET booking API
    Given "booking-svc" service mock for api method "get-booking" using template "<template>"
    When booking details will be fetched for "<bookingId>"
    When "get-booking" endpoint is invoked
    Then the response code should be "<responseCode>"
    And the response body should contain message "<responseBody>"

    Examples:
      | template              | responseCode | responseBody          | bookingId |
      | bad-request           | 400          | bad-request           | 1         |
      | internal-server-error | 500          | internal-server-error | 2         |
```

The `Given` step is provided by `MockSetupGlueSteps` from this library. The `When` and `Then` steps are implemented in the project's own step definition classes under `com.wiremock.utility.test.stepdef`.

---

### Step Definitions

Place project-specific step definitions under `src/test/java/com/wiremock/utility/test/steps/` (or `stepdef/`). The `Given` mock setup step is already provided by the library — only `@When` and `@Then` steps need to be implemented per project.

```java
package com.wiremock.utility.test.steps;

import com.wiremock.utility.MockServerFactory;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class BookingSteps {

    private String bookingId;
    private Response response;

    @When("booking details will be fetched for {string}")
    public void bookingDetailsWillBeFetchedFor(String bookingId) {
        this.bookingId = bookingId;
    }

    @When("{string} endpoint is invoked")
    public void endpointIsInvoked(String endpoint) {
        String baseUrl = MockServerFactory.getBaseUrl();
        response = RestAssured.get(baseUrl + "/booking/" + bookingId);
    }

    @Then("the response code should be {string}")
    public void theResponseCodeShouldBe(String expectedCode) {
        response.then().statusCode(Integer.parseInt(expectedCode));
    }

    @Then("the response body should contain message {string}")
    public void theResponseBodyShouldContainMessage(String expectedMessage) {
        response.then().body("msg", equalTo(expectedMessage));
    }
}
```

---

### Serenity Configuration

**File:** `src/test/resources/serenity.conf`

Uses [HOCON](https://github.com/lightbend/config) format. Supports named environments selected via `-Denvironment=<name>`.

```hocon
environments {
    default {
        app.baseURL         = "http://localhost:8081"
        wiremock_host       = "localhost"
        wiremock_port       = "9090"
        wiremock_protocol   = "http"
    }
}
```

Add further environments (`staging`, `prod`, etc.) as additional blocks and select them at runtime:

```bash
.\gradlew.bat test -Denvironment=staging
```

---

### Logging Configuration

**File:** `src/test/resources/logback-test.xml`

Overrides the main `logback.xml` during test runs. Sets `com.wiremock.utility.test` to `INFO` and root to `WARN` to keep test output readable.

```xml
<logger name="com.wiremock.utility.test" level="INFO"/>
<root level="WARN">
    <appender-ref ref="STDOUT"/>
</root>
```

---

### Running BDD Tests

> **`gradle build` does NOT run tests** — the `test` task is excluded from the build lifecycle via `onlyIf`. Always use `gradle test` (or the commands below) to run tests explicitly.

**Run all scenarios:**

```bash
# Windows
.\gradlew.bat test

# Linux / macOS
./gradlew test
```

**Run with a specific tag:**

```bash
.\gradlew.bat test -Dtags="@booking"
.\gradlew.bat test -Dtags="@smoke and not @wip"
```

**Run against a specific environment:**

```bash
.\gradlew.bat test -Denvironment=staging
```

**Run against a remote WireMock server:**

```bash
.\gradlew.bat test -Dwiremock_host=my-wiremock.example.com \
                   -Dwiremock_port=443 \
                   -Dwiremock_protocol=https
```

**Combine options:**

```bash
.\gradlew.bat test -Dtags="@booking" -Denvironment=staging -Dwiremock_host=my-wiremock.example.com
```

---

### Serenity Report

After `gradle test` completes, `SerenityReporterParallel` automatically aggregates all results and generates a full HTML report:

```
target/site/serenity/index.html
```

The report includes:
- Pass / fail / pending counts per feature and scenario
- Step-by-step execution detail with screenshots (if a web driver is configured)
- Execution timeline at `build/test-results/timeline/`
- Cucumber JSON output at `target/cucumber-reports/`

---

## BDD Usage in Consumer Projects

To use this library's mock setup steps in another project, add the fat JAR as a dependency and configure the Cucumber runner.

### Glue Path Setup

Include `com.wiremock.utility.bdd` alongside your own glue packages in the runner:

```java
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(
    key = GLUE_PROPERTY_NAME,
    value = "com.wiremock.utility.bdd, com.myproject.steps"
)
@ConfigurationParameter(
    key = PLUGIN_PROPERTY_NAME,
    value = "io.cucumber.core.plugin.SerenityReporterParallel, pretty"
)
public class MyProjectRunner {}
```

---

### Writing Feature Steps

**No data — uses all defaults from `default.properties`:**

```gherkin
Given "bank-fn-svc" service mock for api method "callback-account-info"
```

**Custom template:**

```gherkin
Given "bank-fn-svc" service mock for api method "callback-account-info" using template "inactive-account"
```

**With data table — overrides specific tokens, one stub per row:**

```gherkin
Given "bank-fn-svc" service mock for api method "callback-account-info" with data
  | accountId  | status   | balance |
  | 1111111111 | INACTIVE | 0.00    |
  | 2222222222 | ACTIVE   | 500.00  |
```

**Custom template with data table:**

```gherkin
Given "bank-fn-svc" service mock for api method "callback-account-info" using template "custom" with data
  | accountId  | status |
  | 3333333333 | CLOSED |
```

---

### Stub Lifecycle

The library manages stub cleanup automatically — no manual teardown is needed:

| Hook | What happens |
|------|-------------|
| `@Before` | Clears the internal `scenarioStubs` tracking list. No stubs on the WireMock server are touched. |
| `@After` | Calls `wireMock().removeStubMapping(stub)` for every stub registered during the scenario. Pre-existing stubs on a shared/remote server are **not** affected. |

---

## Programmatic Usage

For non-BDD tests (plain JUnit 5) call `MockFactory` directly:

```java
import com.wiremock.utility.MockFactory;
import com.wiremock.utility.MockServerFactory;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

class BookingServiceTest {

    private final List<StubMapping> stubs = new ArrayList<>();

    @AfterEach
    void cleanup() {
        stubs.forEach(stub -> MockServerFactory.wireMock().removeStubMapping(stub));
        stubs.clear();
    }

    @Test
    void shouldReturnSuccessResponse() {
        stubs.add(MockFactory.createMock("booking-svc", "get-booking", "success", null));
        // call service at MockServerFactory.getBaseUrl() + "/booking/123"
    }

    @Test
    void shouldHandleBadRequest() {
        stubs.add(MockFactory.createMock("booking-svc", "get-booking", "bad-request", null));
        // assert 400 response
    }
}
```

---

## Logging

Two logback configurations are provided:

| File | Used when | Root level |
|------|-----------|-----------|
| `src/main/resources/logback.xml` | Normal execution / fat JAR | `INFO` → console |
| `src/test/resources/logback-test.xml` | `gradle test` | `WARN` → console only |

**Main (`logback.xml`) logger levels:**

| Logger | Level | Output |
|--------|-------|--------|
| `com.wiremock.utility` | `DEBUG` | Console + rolling file (`logs/wiremock-utility.log`) |
| `com.github.tomakehurst.wiremock` | `WARN` | Suppresses WireMock's internal verbose output |
| Root | `INFO` | Console |

Log files roll daily and are retained for **7 days**.

---

## Dependencies

### Main (shipped in the fat JAR)

| Dependency | Version |
|-----------|---------|
| `org.wiremock:wiremock-standalone` | 3.13.2 |
| `org.slf4j:slf4j-api` | 2.0.16 |
| `org.apache.logging.log4j:log4j-slf4j2-impl` | 2.23.1 |
| `commons-io:commons-io` | 2.16.1 |
| `org.projectlombok:lombok` | 1.18.38 |
| `com.fasterxml.jackson.core:jackson-databind` | 2.19.0 |
| `org.hamcrest:hamcrest-all` | 1.3 |
| `io.cucumber:cucumber-java` | 7.22.1 |
| `io.cucumber:cucumber-junit-platform-engine` | 7.22.1 |

### Test only (not in fat JAR)

| Dependency | Version |
|-----------|---------|
| `net.serenity-bdd:serenity-core` | 4.2.34 |
| `net.serenity-bdd:serenity-cucumber` | 4.2.34 |
| `net.serenity-bdd:serenity-rest-assured` | 4.2.34 |
| `org.junit.jupiter:junit-jupiter-api` | 5.12.2 |
| `org.junit.jupiter:junit-jupiter-engine` | 5.12.2 |
| `org.junit.platform:junit-platform-suite-api` | 1.12.2 |
| `org.junit.platform:junit-platform-suite` | 1.12.2 |
| `org.junit.platform:junit-platform-launcher` | 1.12.2 |
| `org.junit.platform:junit-platform-suite-engine` | 1.12.2 |
| `io.rest-assured:rest-assured` | 5.5.2 |
| `ch.qos.logback:logback-classic` | 1.5.18 |
