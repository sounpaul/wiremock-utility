# WireMock Utility

A shared Java library that simplifies setting up and managing [WireMock](https://wiremock.org/) stub servers in automated test suites. It provides a file-driven templating system for defining mock responses, a singleton server factory, and ready-to-use Cucumber BDD step definitions.

---

## Table of Contents

- [Requirements](#requirements)
- [Build](#build)
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
- [BDD Usage (Cucumber)](#bdd-usage-cucumber)
- [Programmatic Usage](#programmatic-usage)
- [Logging](#logging)
- [Dependencies](#dependencies)

---

## Requirements

| Tool | Version |
|------|---------|
| Java | 21+ |
| Gradle (via wrapper) | 8.8 |

No global Gradle installation is needed. Use `gradlew` / `gradlew.bat`.

---

## Build

```bash
# Windows
.\gradlew.bat compileJava

# Linux / macOS
./gradlew compileJava
```

To publish to a local Maven repository:

```bash
.\gradlew.bat publishToMavenLocal
```

---

## Architecture Overview

```
Test / Feature file
       │
       │  @Given step
       ▼
MockSetupGlueSteps          ← Cucumber glue (BDD entry point)
       │
       │  createMock(service, apiMethod, template, values)
       ▼
  MockFactory                ← Loads & renders template files from classpath
       │  reads: mock.json, body.json, default.properties
       │  applies token replacement + value translation
       │
       │  register(StubMapping)
       ▼
MockServerFactory            ← Singleton WireMock client / server manager
       │
       ▼
  WireMock server            ← Local (auto-started) or remote (PCF/cloud)
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
- If host is `localhost`: creates and starts a `WireMockServer` on port **9081**. The client connects to `http://localhost:9081`. If the server is already running (`FatalStartupException`), it logs a warning and continues.
- If host is anything else: connects to the remote WireMock server using `wiremock_protocol`, `wiremock_host`, and `wiremock_port` (no local server is started).

---

### MockFactory

**Package:** `com.wiremock.utility`

Loads stub definitions from classpath resources, performs token substitution, and registers them with the WireMock client.

```java
// Register a stub with no dynamic data (uses default.properties only)
MockFactory.createMock("my-service", "get-account", "default", null);

// Register a stub with runtime overrides
Map<String, String> overrides = Map.of("accountId", "12345", "status", "ACTIVE");
MockFactory.createMock("my-service", "get-account", "default", overrides);
```

**How it works:**

1. Builds the resource path: `mocks/{service}/{apiMethod}/{template}/`
2. Reads `body.json` and JSON-encodes it (escaping quotes/special characters).
3. Reads `default.properties` and merges any runtime `values` map on top.
4. Reads `mock.json` and replaces `[[body]]` with the encoded body string.
5. Replaces every `[[propertyName]]` token with the value from the merged properties, passing each value through the `translate()` function.
6. Registers the rendered stub via `MockServerFactory.wireMock().register(StubMapping.buildFrom(...))`.

---

### MockSetupGlueSteps

**Package:** `com.wiremock.utility.bdd`

Cucumber step definitions that delegate directly to `MockFactory.createMock()`. Intended to be added to the Cucumber glue path in consumer projects.

| Annotation | Step Pattern |
|-----------|-------------|
| `@Before` | *(runs before every scenario)* — resets all WireMock mappings |
| `@Given` | `"{string} service mock for api method {string}"` |
| `@Given` | `"{string} service mock for api method {string} using template {string}"` |
| `@Given` | `"{string} service mock for api method {string} with data"` |
| `@Given` | `"{string} service mock for api method {string} using template {string} with data"` |

---

## Resource File Structure

All mock resource files must be placed under `src/main/resources/mocks/` (or `src/test/resources/mocks/` in consumer projects) following this layout:

```
src/main/resources/
└── mocks/
    └── {service}/
        └── {apiMethod}/
            └── {template}/
                ├── mock.json           ← WireMock stub mapping
                ├── body.json           ← Raw response body
                └── default.properties  ← Default token values
```

**Example** for `service=bank-fn-svc`, `apiMethod=callback-account-info`, `template=default`:

```
mocks/
└── bank-fn-svc/
    └── callback-account-info/
        └── default/
            ├── mock.json
            ├── body.json
            └── default.properties
```

---

### mock.json

A standard WireMock stub mapping file. Use `[[body]]` where the response body should be injected, and `[[tokenName]]` for any other dynamic values.

```json
{
  "request": {
    "method": "GET",
    "urlPathPattern": "/api/v1/accounts/[[accountId]]"
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

---

### body.json

The raw (unescaped) JSON response body. Write it as plain JSON — `MockFactory` handles the escaping automatically before injecting it into `mock.json`.

```json
{
  "accountId": "[[accountId]]",
  "status": "[[status]]",
  "balance": "[[balance]]"
}
```

---

### default.properties

Key-value pairs providing the default values for all `[[tokens]]` used in `mock.json` and `body.json`. At runtime these can be selectively overridden by passing a `Map<String, String>` to `createMock()`.

```properties
accountId=9999999999
status=ACTIVE
balance=1000.00
```

---

### Template Tokens

Tokens follow the pattern `[[tokenName]]` in both `mock.json` and `body.json`. At render time, `MockFactory` replaces every token whose name matches a key in the merged properties.

| Token in file | Key in properties | Resolved value |
|---------------|-------------------|----------------|
| `[[accountId]]` | `accountId` | value from properties / override map |
| `[[status]]` | `status` | value from properties / override map |
| `[[body]]` | *(reserved)* | rendered content of `body.json` |

---

### Translate Special Values

Property values pass through a `translate()` function before substitution. This allows feature files and property files to use human-readable keywords for edge-case values:

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
| `wiremock_port` | `443` | WireMock server port (used only when connecting to a remote host). |
| `wiremock_protocol` | `https` | Protocol for remote connections (`http` or `https`). |

When `wiremock_host=localhost`, the local server always starts on port **9081** regardless of `wiremock_port`.

**Running against a remote WireMock server (e.g. on PCF/cloud):**

```bash
.\gradlew.bat test -Dwiremock_host=my-wiremock-host.example.com \
                   -Dwiremock_port=443 \
                   -Dwiremock_protocol=https
```

---

## BDD Usage (Cucumber)

### 1. Add the glue path

In your Cucumber runner, include `com.wiremock.utility.bdd` in the glue paths:

```java
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.wiremock.utility.bdd, com.myproject.steps")
public class CucumberRunner {}
```

### 2. Write feature file steps

**No data (uses all defaults from `default.properties`):**

```gherkin
Given "bank-fn-svc" service mock for api method "callback-account-info"
```

**Custom template:**

```gherkin
Given "bank-fn-svc" service mock for api method "callback-account-info" using template "inactive-account"
```

**With data table (overrides specific tokens per row):**

```gherkin
Given "bank-fn-svc" service mock for api method "callback-account-info" with data
  | accountId  | status   | balance |
  | 1111111111 | INACTIVE | 0.00    |
  | 2222222222 | ACTIVE   | 500.00  |
```

Each row in the data table registers a separate stub mapping.

**Custom template with data:**

```gherkin
Given "bank-fn-svc" service mock for api method "callback-account-info" using template "custom" with data
  | accountId  | status |
  | 3333333333 | CLOSED |
```

### 3. Automatic reset

`MockSetupGlueSteps` is annotated with `@Before`, so all registered mappings are cleared before every scenario — no manual teardown needed.

---

## Programmatic Usage

For non-BDD tests (JUnit 5, etc.) call `MockFactory` directly:

```java
import com.wiremock.utility.MockFactory;
import com.wiremock.utility.MockServerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MyServiceTest {

    @BeforeEach
    void resetMocks() {
        MockServerFactory.wireMock().resetMappings();
    }

    @Test
    void shouldReturnActiveAccount() {
        MockFactory.createMock("bank-fn-svc", "callback-account-info", "default", null);
        // ... call the service under test
    }

    @Test
    void shouldHandleInactiveAccount() {
        Map<String, String> overrides = Map.of("status", "INACTIVE", "balance", "0.00");
        MockFactory.createMock("bank-fn-svc", "callback-account-info", "default", overrides);
        // ... call the service under test
    }
}
```

The WireMock server base URL (for configuring your HTTP client) is available via:

```java
String baseUrl = MockServerFactory.getBaseUrl(); // e.g. "http://localhost:9081"
```

---

## Logging

Logging is configured via [`src/main/resources/logback.xml`](src/main/resources/logback.xml).

| Logger | Level | Output |
|--------|-------|--------|
| `com.wiremock.utility` | `DEBUG` | Console + rolling file (`logs/wiremock-utility.log`) |
| `com.github.tomakehurst.wiremock` | `WARN` | Suppresses WireMock's internal verbose output |
| Root | `INFO` | Console |

Log files roll daily and are retained for **7 days**.

---

## Dependencies

| Dependency | Version | Scope |
|-----------|---------|-------|
| `org.wiremock:wiremock-standalone` | 3.13.2 | implementation |
| `org.slf4j:slf4j-api` | 2.0.16 | implementation |
| `org.apache.logging.log4j:log4j-slf4j2-impl` | 2.23.1 | implementation |
| `commons-io:commons-io` | 2.16.1 | implementation |
| `org.projectlombok:lombok` | 1.18.38 | compileOnly / annotationProcessor |
| `com.fasterxml.jackson.core:jackson-databind` | 2.17.1 | implementation |
| `org.hamcrest:hamcrest-all` | 1.3 | implementation |
| `io.cucumber:cucumber-java` | 7.22.1 | implementation |
| `io.cucumber:cucumber-junit-platform-engine` | 7.22.1 | implementation |
