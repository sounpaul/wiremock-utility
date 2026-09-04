# Project Context: wiremock-utility

This document is a complete, self-contained specification of the `wiremock-utility` project. It is written so that an AI coding assistant (e.g., GitHub Copilot / Copilot Chat / Copilot Workspace) in a **different, empty repository** can regenerate this project file-for-file, including exact package names, class contents, resource files, and build configuration.

Feed this entire document to the target system as the task brief, then ask it to scaffold the files in the order given under "Build Order".

---

## 1. Purpose & Summary

`wiremock-utility` is a **Java 21 shared library** that simplifies setting up and managing [WireMock](https://wiremock.org/) stub servers inside automated test suites. It provides:

1. A **file-driven templating system** for defining WireMock stub mappings (`mock.json` + `body.json` + `default.properties` per template folder), with `[[token]]` placeholder substitution and runtime overrides.
2. A **thread-safe singleton** (`MockServerFactory`) that manages a single `WireMock` API client for the whole JVM/test run.
3. Ready-to-use **Cucumber BDD step definitions** (`MockSetupGlueSteps`) that consumer projects can pull in via Cucumber "glue" packages, so they get stub-mapping `Given` steps for free.
4. A self-contained **demo/verification test suite** (Serenity BDD + Cucumber + JUnit 5 Platform + REST Assured) that exercises the library against a fictitious "booking-svc" API, proving the library works end-to-end.

The library is packaged as a **fat/uber JAR** (`wiremock-utility-1.0.0-all.jar`) so consumer projects can drop in one dependency and get WireMock, Jackson, Cucumber, SLF4J, Lombok, etc. bundled.

Group: `com.wiremock.utility` — Version: `1.0.0` — Root Gradle project name: `wiremock-utility`.

---

## 2. Tech Stack & Exact Dependency Versions

Build tool: **Gradle 8.8** (via wrapper, `gradlew` / `gradlew.bat` — no global Gradle needed).

Java: **21** (`sourceCompatibility`/`targetCompatibility` = `VERSION_21`, toolchain `JavaLanguageVersion.of(21)`).

Gradle plugins applied:
- `java`
- `maven-publish`
- `eclipse`
- `idea`
- `net.serenity-bdd.serenity-gradle-plugin` version matches `serenityCoreVersion` (see below)

### Main scope (shipped inside the fat JAR)

| Dependency | Version | Configuration |
|---|---|---|
| `org.slf4j:slf4j-api` | 2.0.16 | implementation |
| `org.apache.logging.log4j:log4j-slf4j2-impl` | 2.23.1 | implementation |
| `commons-io:commons-io` | 2.16.1 | implementation |
| `org.projectlombok:lombok` | 1.18.38 | compileOnly + annotationProcessor |
| `org.hamcrest:hamcrest-all` | 1.3 | implementation |
| `com.fasterxml.jackson.core:jackson-databind` | 2.19.0 | implementation |
| `org.wiremock:wiremock-standalone` | 3.13.2 | implementation |
| `io.cucumber:cucumber-java` | 7.22.1 | implementation |
| `io.cucumber:cucumber-junit-platform-engine` | 7.22.1 | implementation |

### Test scope (not in fat JAR)

| Dependency | Version |
|---|---|
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
| `org.projectlombok:lombok` | 1.18.38 (testCompileOnly + testAnnotationProcessor) |

Gradle version catalog is implemented via `buildscript { ext { ... } }` variables (not a `libs.versions.toml`), e.g. `wiremockVersion`, `slf4jVersion`, `lombokVersion`, `jacksonVersion`, `commonsIoVersion`, `ioCucumberVersion`, `serenityCoreVersion`, `restAssuredVersion`, `logbackVersion`, `junitPlatformVersion`, `junitJupiterVersion`.

---

## 3. Repository Layout

```
wiremock-utility/
├── .gitignore
├── README.md
├── build.gradle
├── settings.gradle
├── serenity.properties
├── gradlew / gradlew.bat
├── gradle/wrapper/gradle-wrapper.jar
├── src/
│   ├── main/
│   │   ├── java/com/wiremock/utility/
│   │   │   ├── MockServerFactory.java
│   │   │   ├── MockFactory.java
│   │   │   └── bdd/
│   │   │       └── MockSetupGlueSteps.java
│   │   └── resources/
│   │       └── logback.xml
│   └── test/
│       ├── java/com/wiremock/utility/test/
│       │   ├── client/BookingAPIClient.java
│       │   ├── hooks/Hooks.java
│       │   ├── model/BookingDates.java
│       │   ├── model/BookingRequest.java
│       │   ├── runner/BookingAPIRunner.java
│       │   ├── stepdef/BookingAPIStepDef.java
│       │   ├── steps/BookingAPISteps.java
│       │   └── utils/TestConstants.java
│       └── resources/
│           ├── features/MockUtilityTest.feature
│           ├── junit-platform.properties
│           ├── logback-test.xml
│           ├── serenity.conf
│           └── mocks/booking-svc/
│               ├── create-booking/default/{mock.json,body.json,default.properties}
│               └── get-booking/
│                   ├── default/{mock.json,body.json,default.properties}
│                   ├── bad-request/{mock.json,body.json,default.properties}
│                   └── internal-server-error/{mock.json,body.json,default.properties}
```

`.gitignore` excludes: `.gradle/`, `build/`, `out/`, `*.class`, `*.jar` (except `gradle/wrapper/gradle-wrapper.jar`), IDE files (`.idea/`, `*.iml`), `wiremock-binary/`, `target/`.

---

## 4. Build Order (recommended sequence for regeneration)

1. `settings.gradle`, `build.gradle`, `.gitignore`, `gradlew`/`gradlew.bat` wrapper.
2. `src/main/java/com/wiremock/utility/MockServerFactory.java`
3. `src/main/java/com/wiremock/utility/MockFactory.java`
4. `src/main/java/com/wiremock/utility/bdd/MockSetupGlueSteps.java`
5. `src/main/resources/logback.xml`
6. Test resource mock templates (`src/test/resources/mocks/**`)
7. `src/test/resources/features/MockUtilityTest.feature`
8. `src/test/java/.../model/BookingDates.java`, `BookingRequest.java`
9. `src/test/java/.../utils/TestConstants.java`
10. `src/test/java/.../client/BookingAPIClient.java`
11. `src/test/java/.../steps/BookingAPISteps.java`
12. `src/test/java/.../stepdef/BookingAPIStepDef.java`
13. `src/test/java/.../hooks/Hooks.java`
14. `src/test/java/.../runner/BookingAPIRunner.java`
15. `src/test/resources/{serenity.conf, junit-platform.properties, logback-test.xml}`
16. `serenity.properties`
17. `README.md`

---

## 5. Full Gradle Configuration

### settings.gradle

```gradle
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = 'wiremock-utility'
```

### build.gradle

```gradle
buildscript {
    ext {
        // Core utility
        wiremockVersion = '3.13.2'
        slf4jVersion = '2.0.16'
        lombokVersion = '1.18.38'
        jacksonVersion = '2.19.0'
        commonsIoVersion = '2.16.1'

        // BDD / Test
        ioCucumberVersion = '7.22.1'
        serenityCoreVersion = '4.2.34'
        restAssuredVersion = '5.5.2'
        logbackVersion = '1.5.18'

        // JUnit 5 (platform + jupiter versions must be aligned)
        junitPlatformVersion = '1.12.2'
        junitJupiterVersion = '5.12.2'
    }
    repositories {
        mavenCentral()
    }
}

plugins {
    id 'java'
    id 'maven-publish'
    id 'eclipse'
    id 'idea'
    id 'net.serenity-bdd.serenity-gradle-plugin' version "${serenityCoreVersion}"
}

group = 'com.wiremock.utility'
version = '1.0.0'

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

apply plugin: "net.serenity-bdd.serenity-gradle-plugin"

dependencies {
    // ── Main ─────────────────────────────────────────────────────────────────
    implementation "org.slf4j:slf4j-api:${slf4jVersion}"
    implementation 'org.apache.logging.log4j:log4j-slf4j2-impl:2.23.1'
    implementation "commons-io:commons-io:${commonsIoVersion}"
    compileOnly "org.projectlombok:lombok:${lombokVersion}"
    annotationProcessor "org.projectlombok:lombok:${lombokVersion}"
    implementation 'org.hamcrest:hamcrest-all:1.3'
    implementation "com.fasterxml.jackson.core:jackson-databind:${jacksonVersion}"
    implementation "org.wiremock:wiremock-standalone:${wiremockVersion}"

    // Cucumber (main scope — glue steps shipped as part of this library)
    implementation "io.cucumber:cucumber-java:${ioCucumberVersion}"
    implementation "io.cucumber:cucumber-junit-platform-engine:${ioCucumberVersion}"

    // ── Test ─────────────────────────────────────────────────────────────────
    // Serenity BDD
    testImplementation "net.serenity-bdd:serenity-core:${serenityCoreVersion}"
    testImplementation "net.serenity-bdd:serenity-cucumber:${serenityCoreVersion}"
    testImplementation "net.serenity-bdd:serenity-rest-assured:${serenityCoreVersion}"

    // JUnit 5 — Jupiter API (compile) + Engine (runtime)
    testImplementation "org.junit.jupiter:junit-jupiter-api:${junitJupiterVersion}"
    testImplementation "org.junit.jupiter:junit-jupiter-engine:${junitJupiterVersion}"

    // JUnit Platform — Suite annotations (compile) + engines (runtime)
    testImplementation "org.junit.platform:junit-platform-suite-api:${junitPlatformVersion}"
    testImplementation "org.junit.platform:junit-platform-suite:${junitPlatformVersion}"
    testImplementation "org.junit.platform:junit-platform-launcher:${junitPlatformVersion}"
    testImplementation "org.junit.platform:junit-platform-suite-engine:${junitPlatformVersion}"

    // Logging for tests
    testImplementation "ch.qos.logback:logback-classic:${logbackVersion}"

    // Lombok for test sources
    testCompileOnly "org.projectlombok:lombok:${lombokVersion}"
    testAnnotationProcessor "org.projectlombok:lombok:${lombokVersion}"
}

// disable the default thin JAR — only the fat JAR is needed
jar.enabled = false

// fat JAR — bundles all runtime dependencies into a single self-contained JAR
task fatJar(type: Jar) {
    archiveBaseName = 'wiremock-utility'
    archiveVersion = "${project.version}"
    archiveClassifier = 'all'

    manifest {
        attributes(
                'Implementation-Title': project.name,
                'Implementation-Version': project.version,
                'Implementation-Vendor': 'com.wiremock.utility',
                'Built-By': System.getProperty('user.name'),
                'Build-Jdk': System.getProperty('java.version'),
                'Created-By': "Gradle ${gradle.gradleVersion}"
        )
    }

    from sourceSets.main.output
    dependsOn configurations.runtimeClasspath
    from {
        configurations.runtimeClasspath
                .findAll { it.name.endsWith('jar') }
                .collect { zipTree(it) }
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// hook fatJar into the standard build lifecycle
build.dependsOn fatJar

test {
    // Only execute when 'test' is explicitly requested on the command line.
    // 'gradle build' will skip this task; 'gradle test' will run it normally.
    onlyIf {
        gradle.startParameter.taskNames.any { it == 'test' || it == ':test' }
    }

    useJUnitPlatform()
    systemProperties(System.getProperties())
    testLogging {
        events 'passed', 'skipped', 'failed'
        exceptionFormat 'full'
        showStandardStreams = false
    }

    // Generate Serenity aggregate report after tests complete
    finalizedBy('aggregate')
}
```

**Key build behaviours to preserve:**
- `jar.enabled = false` — the default thin JAR is disabled; only `fatJar` is produced.
- `build.dependsOn fatJar` — running `gradle build` produces `build/libs/wiremock-utility-1.0.0-all.jar`.
- The `test` task uses `onlyIf` so that `gradle build` **never** runs tests — tests must be triggered explicitly via `gradle test`.
- `systemProperties(System.getProperties())` forwards all `-D` JVM args from the Gradle invocation into the test JVM (this is how `-Dwiremock_host=...`, `-Denvironment=...`, `-Dtags=...` reach the tests).
- `finalizedBy('aggregate')` triggers the Serenity reporting aggregation task after `test`.

### serenity.properties (project root)

```properties
serenity.project.name=WireMock Utility - Booking API Tests
```

### gitignore

```
.gradle/
build/
out/
*.class
*.jar
!gradle/wrapper/gradle-wrapper.jar
.idea/workspace.xml
.idea/tasks.xml
.idea/.gitignore
.idea/shelf/
*.iml
.idea/
wiremock-binary/
target/
```

---

## 6. Main Library Source (`src/main/java`)

### 6.1 `com.wiremock.utility.MockServerFactory`

Thread-safe singleton wrapping a WireMock API client (`com.github.tomakehurst.wiremock.client.WireMock`).

```java
package com.wiremock.utility;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.FatalStartupException;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MockServerFactory {

    private static WireMock wireMockClient;
    private static String mockBaseUrl;
    private static final int defaultLocalPort = 9081;
    private static WireMockServer server;

    private MockServerFactory() {
    }

    public static synchronized WireMock wireMock() {
        if (wireMockClient != null) {
            return wireMockClient;
        } else {
            String mockServerHost = System.getProperty("wiremock_host", "localhost");
            int mockServerPort = Integer.parseInt(System.getProperty("wiremock_port", "443"));
            String mockServerProtocol = System.getProperty("wiremock_protocol", "https");
            log.info("Connecting to remote WireMock at {}://{}:{}", mockServerProtocol, mockServerHost, mockServerPort);
            wireMockClient = new WireMock(mockServerProtocol, mockServerHost, mockServerPort);

            mockBaseUrl = mockServerProtocol + "://" + mockServerHost + ":" + mockServerPort;
            log.info("WireMock base URL set to {}", mockBaseUrl);
            return wireMockClient;
        }
    }

    public static String getBaseUrl() {
        return mockBaseUrl;
    }

    public static WireMockServer getWireMockServer() {
        wireMock();
        return server;
    }
}
```

**Behavioural notes (IMPORTANT — reproduce exactly, this is the current, real behaviour of the code, not what the README describes):**
- `wireMock()` is `synchronized` and memoizes into a static field — first caller wins for the whole JVM.
- Defaults if system properties are absent: `wiremock_host=localhost`, `wiremock_port=443`, `wiremock_protocol=https`.
- **It always connects as a client to whatever host/port/protocol is configured — it never starts an in-process `WireMockServer`.** There is a `WireMockServer server` field and a `defaultLocalPort = 9081` constant, and `getWireMockServer()` calls `wireMock()` then returns the (always-null) `server` field, but nothing in the class ever assigns `server` or calls `.start()`. This is dead/vestigial state — keep it present (for API compatibility / to match this codebase exactly) but do not wire it up unless separately asked to fix it.
- Practical implication: a consumer of this library **must point `wiremock_host`/`wiremock_port`/`wiremock_protocol` (or the environment-specific equivalents in `serenity.conf`) at an already-running WireMock instance** (local process started independently, container, or remote server). The library itself does not spin one up despite the class name.

### 6.2 `com.wiremock.utility.MockFactory`

Loads template files from the classpath, performs token substitution, and registers the resulting `StubMapping`.

```java
package com.wiremock.utility;

import com.fasterxml.jackson.core.io.JsonStringEncoder;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

@Slf4j
public class MockFactory {

    public MockFactory() {
    }

    public static void createMock(String service, String apiMethod, String template, Map<String, String> values) {
        String templatePath = "mocks/" + service + "/" + apiMethod + "/" + template + "/";
        String nonEscapedBodyStr = getStringFromFile(templatePath + "body.json");
        String bodyStr = new String(JsonStringEncoder.getInstance().quoteAsString(nonEscapedBodyStr));
        Properties props = loadAndMergeProperties(templatePath + "default.properties", values);
        String stubMappingStr = getStringFromFile(templatePath + "mock.json");
        stubMappingStr = stubMappingStr.replace("[[body]]", bodyStr);

        for (String propName : props.stringPropertyNames()) {
            stubMappingStr =
                    stubMappingStr.replace("[[" + propName + "]]", translate(props.getProperty(propName)));
        }

        StubMapping stubMapping = StubMapping.buildFrom(stubMappingStr);
        MockServerFactory.wireMock().register(stubMapping);
        log.debug("Registered stub mapping with id {} for service={}, apiMethod={}, template={}",
                stubMapping.getId(), service, apiMethod, template);
    }

    private static String getStringFromFile(String filePath) {
        String jsonName = filePath.replace(".xml", ".json");
        String xmlName = filePath.replace(".json", ".xml");
        String correctFile = jsonName;
        URL f = ClassLoader.getSystemClassLoader().getResource(jsonName);

        if (f == null) {
            f = ClassLoader.getSystemClassLoader().getResource(xmlName);
            correctFile = xmlName;
        }

        if (f == null) {
            log.error("File does not exist with a .xml or .json extension - {}", filePath);
            return "";
        } else {
            try (InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream(correctFile)) {
                return IOUtils.toString(in, "utf-8");
            } catch (IOException e) {
                log.error("Error in reading file {}", f.getFile(), e);
                return "";
            }
        }
    }

    private static Properties loadAndMergeProperties(String defaultPropsFilePath, Map<String, String> values) {
        Properties props = new Properties();

        try {
            props.load(ClassLoader.getSystemClassLoader().getResourceAsStream(defaultPropsFilePath));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (values != null) {
            props.putAll(values);
        }

        return props;
    }

    private static String translate(String in) {
        String inLow = in.toLowerCase();
        if (inLow.equals("empty")) {
            return "";
        } else if (inLow.equals("empty string")) {
            return "";
        } else if (inLow.equals("space")) {
            return " ";
        } else {
            return inLow.matches("\\d{1,2} space[s]{0,1}") ? space(inLow.split(" ")[0]) : in;
        }
    }

    private static String space(String countStr) {
        return " ".repeat(Integer.parseInt(countStr));
    }
}
```

**Behavioural notes:**
- Resource path convention: `mocks/{service}/{apiMethod}/{template}/{mock.json|body.json|default.properties}`, loaded via `ClassLoader.getSystemClassLoader()` (classpath resources — so these must live under `src/test/resources` or `src/main/resources` of the *consuming* module, packaged/available on the test classpath).
- `body.json` is read raw, then JSON-string-escaped via Jackson's `JsonStringEncoder.quoteAsString(...)` before being spliced into `mock.json` at the `[[body]]` token — this is what lets `body.json` be written as normal, readable JSON instead of a hand-escaped string.
- Property values are merged: `default.properties` loaded first, then the runtime `values` map (if non-null) is layered on top via `Properties.putAll`, so runtime values win.
- Every `[[propName]]` token in `mock.json` (already containing the substituted body) is replaced by the corresponding property value **after** passing it through `translate()`.
- `translate()` special-cases (case-insensitive): `"empty"` / `"empty string"` → `""`; `"space"` → `" "`; a value matching `\d{1,2} space[s]?` (e.g. `"3 spaces"`, `"1 space"`) → that many literal spaces; anything else is returned unchanged (original casing preserved — only the match check is lower-cased).
- `getStringFromFile` has a quirk: it supports both `.json` and `.xml` template extensions by trying whichever the caller passed as `.json`, and falling back to the `.xml` sibling name if the `.json` resource isn't found (and vice versa) — reproduce this exact fallback logic even though only `.json` files exist in this repo today.
- No caching — every `createMock()` call re-reads all three files from the classpath.

### 6.3 `com.wiremock.utility.bdd.MockSetupGlueSteps`

Cucumber glue class exposing 4 `@Given` patterns plus a scenario-reset `@Before`.

```java
package com.wiremock.utility.bdd;

import com.wiremock.utility.MockServerFactory;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static com.wiremock.utility.MockFactory.createMock;

@Slf4j
public class MockSetupGlueSteps {

    @Before
    public void beforeScenario() {
        MockServerFactory.wireMock().resetMappings();
    }

    @Given("{string} service mock for api method {string}")
    public void given_mock_with_default_template_and_no_data(String service, String apiMethod) {
        createMockNoData(service, apiMethod, "default");
    }

    @Given("{string} service mock for api method {string} using template {string}")
    public void given_mock_with_template_and_no_data(String service, String apiMethod, String template) {
        createMockNoData(service, apiMethod, template);
    }

    @Given("{string} service mock for api method {string} with data")
    public void given_mock_with_default_template_and_data(String service, String apiMethod, DataTable table) {
        Map<String, String> values = table.asMaps(String.class, String.class).getFirst();
        createMockWithData(service, apiMethod, "default", values);
    }

    @Given("{string} service mock for api method {string} using template {string} with data")
    public void given_mock_with_template_and_data(String service, String apiMethod, String template, DataTable table) {
        Map<String, String> values = table.asMaps(String.class, String.class).getFirst();
        createMockWithData(service, apiMethod, template, values);
    }

    private void createMockWithData(String service, String apiMethod, String template, Map<String, String> values) {
        createMock(service, apiMethod, template, values);
    }

    private void createMockNoData(String service, String apiMethod, String template) {
        createMock(service, apiMethod, template, null);
    }
}
```

**Behavioural notes:**
- The "default template" convention: when no `using template "<name>"` clause is given in the feature step, the template folder literally named `default` is used (e.g. `mocks/booking-svc/get-booking/default/`).
- `@Before` (no `order` specified) wipes **all** WireMock stub mappings before every scenario, regardless of the previous scenario's outcome — guarantees test isolation without per-stub cleanup.
- Data-table steps take only the **first row** of the table (`.getFirst()`) as the single override map — these are not designed for multi-row/multi-stub registration in one step.

### 6.4 `src/main/resources/logback.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>

  <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
      <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
    </encoder>
  </appender>

  <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>logs/wiremock-utility.log</file>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>logs/wiremock-utility.%d{yyyy-MM-dd}.log</fileNamePattern>
      <maxHistory>7</maxHistory>
    </rollingPolicy>
    <encoder>
      <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
    </encoder>
  </appender>

  <logger name="com.wiremock.utility" level="DEBUG" additivity="false">
    <appender-ref ref="CONSOLE"/>
    <appender-ref ref="FILE"/>
  </logger>

  <logger name="com.github.tomakehurst.wiremock" level="WARN"/>

  <root level="INFO">
    <appender-ref ref="CONSOLE"/>
  </root>

</configuration>
```

---

## 7. Demo/Verification Test Suite (`src/test`)

This is a Serenity BDD + Cucumber + JUnit 5 Platform suite that exercises the library against a fictitious `booking-svc` API. It doubles as living documentation/proof that the library works.

### 7.1 Domain models

`src/test/java/com/wiremock/utility/test/model/BookingDates.java`:

```java
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
```

`src/test/java/com/wiremock/utility/test/model/BookingRequest.java`:

```java
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
```

### 7.2 `src/test/java/com/wiremock/utility/test/utils/TestConstants.java`

```java
package com.wiremock.utility.test.utils;

public class TestConstants {

    public static final String APP_BASE_URI = System.getProperty("app.base.url");
    public static final String GET_BOOKING_ENDPOINT = "/v1/booking/get/{bookingId}";
    public static final String ADD_BOOKING_ENDPOINT = "/v1/booking/create";

}
```

`APP_BASE_URI` is read as a **static final** field at class-load time from the `app.base.url` system property — this means whatever sets that property (see `Hooks` below) **must run before this class is first referenced/loaded** in the JVM.

### 7.3 `src/test/java/com/wiremock/utility/test/client/BookingAPIClient.java`

```java
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
```

Uses `net.serenitybdd.rest.SerenityRest.given()` (Serenity's REST Assured wrapper, for automatic request/response logging in the Serenity report), `relaxedHTTPSValidation()` (skip TLS cert validation against local/self-signed mocks).

### 7.4 `src/test/java/com/wiremock/utility/test/steps/BookingAPISteps.java`

```java
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
```

Note: `validateBookingPayload` verifies against WireMock's recorded request journal on path `/booking` (the WireMock stub's own path from `mocks/booking-svc/create-booking/default/mock.json`), **not** `TestConstants.ADD_BOOKING_ENDPOINT` (`/v1/booking/create`) — these two paths are intentionally/accidentally different; keep both hard-coded values as-is to match current behaviour.

### 7.5 `src/test/java/com/wiremock/utility/test/stepdef/BookingAPIStepDef.java`

```java
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
```

Uses the `@Steps`-injected Serenity pattern: `BookingAPIStepDef` is thin Cucumber glue that delegates every step to a `@Steps`-annotated `BookingAPISteps` "action class", which is how Serenity BDD structures step layers (Cucumber glue → Serenity `@Step` actions) for reporting purposes.

### 7.6 `src/test/java/com/wiremock/utility/test/hooks/Hooks.java`

```java
package com.wiremock.utility.test.hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.model.environment.EnvironmentSpecificConfiguration;
import net.thucydides.model.environment.SystemEnvironmentVariables;
import net.thucydides.model.util.EnvironmentVariables;

@Slf4j
public class Hooks {

    @Before(order = 1)
    public void before(Scenario scenario) {
        log.info("Start of scenario : {}", scenario.getName());
        EnvironmentVariables environmentVariables = SystemEnvironmentVariables.createEnvironmentVariables();
        EnvironmentSpecificConfiguration environmentSpecificConfiguration = EnvironmentSpecificConfiguration.from(environmentVariables);
        System.setProperty("wiremock_host", environmentSpecificConfiguration.getProperty("wiremock_host"));
        System.setProperty("wiremock_port", environmentSpecificConfiguration.getProperty("wiremock_port"));
        System.setProperty("wiremock_protocol", environmentSpecificConfiguration.getProperty("wiremock_protocol"));
        System.setProperty("app.base.url", environmentSpecificConfiguration.getProperty("app.baseURL"));
    }

    @After(order = 10)
    public void after(Scenario scenario) {
        log.info("End of scenario : {}", scenario.getName());
    }

}
```

**Critical wiring:** this `@Before(order = 1)` hook is what bridges Serenity's `serenity.conf` (HOCON, environment-aware) config into the plain JVM system properties (`wiremock_host`, `wiremock_port`, `wiremock_protocol`, `app.base.url`) that `MockServerFactory` and `TestConstants` read. It runs on **every** scenario (re-setting the same values every time), and must execute before `MockSetupGlueSteps`'s own `@Before` (no explicit order = default Cucumber ordering, but since `MockServerFactory.wireMock()` memoizes on first call, and `TestConstants.APP_BASE_URI` is resolved once at class-load — ensure `Hooks` glue package is registered and its `@Before(order=1)` runs early enough relative to first usage). In the runner's `GLUE_PROPERTY_NAME` the packages are listed as `com.wiremock.utility.bdd, com.wiremock.utility.test.stepdef, com.wiremock.utility.test.hooks` — Cucumber sorts `@Before` hooks by `order` value across all glue packages, `order = 1` runs first.

### 7.7 `src/test/java/com/wiremock/utility/test/runner/BookingAPIRunner.java`

```java
package com.wiremock.utility.test.runner;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

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
public class BookingAPIRunner {
}
```

**Important:** all glue packages MUST be listed in a single `GLUE_PROPERTY_NAME` `@ConfigurationParameter` value, comma-separated. Duplicate `@ConfigurationParameter(key = GLUE_PROPERTY_NAME, ...)` annotations are silently overwritten/dropped by the JUnit Platform (only the last one wins) — this is a known JUnit Platform `@Suite` gotcha and must be respected when regenerating.

### 7.8 Feature file — `src/test/resources/features/MockUtilityTest.feature`

```gherkin
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
```

The 4 `Given` step shapes here exactly exercise the 4 `@Given` patterns provided by `MockSetupGlueSteps` (default template / named template / default template + data / named template + data), plus one scenario tagged `@CreateBooking` that additionally exercises WireMock request-verification (`verifyThat`).

### 7.9 Mock template resources (`src/test/resources/mocks/booking-svc/**`)

**`get-booking/default/`** (used when no `using template "..."` clause is given — responds 401):

`mock.json`:
```json
{
  "request": {
    "method": "GET",
    "urlPathPattern": "/booking/[^/]+"
  },
  "response": {
    "status": 401,
    "headers": {
      "Content-Type": "application/json"
    },
    "body": "[[body]]"
  }
}
```
`body.json`:
```json
{
  "msg" : "[[msg]]"
}
```
`default.properties`:
```properties
msg=authorization-error
```

**`get-booking/bad-request/`**:

`mock.json`:
```json
{
  "request": {
    "method": "GET",
    "urlPathPattern": "/booking/[^/]+"
  },
  "response": {
    "status": 400,
    "headers": {
      "Content-Type": "application/json"
    },
    "body": "[[body]]"
  }
}
```
`body.json`:
```json
{
  "msg" : "[[msg]]"
}
```
`default.properties`:
```properties
msg=bad-request
```

**`get-booking/internal-server-error/`**:

`mock.json`:
```json
{
  "request": {
    "method": "GET",
    "urlPathPattern": "/booking/[^/]+"
  },
  "response": {
    "status": 500,
    "headers": {
      "Content-Type": "application/json"
    },
    "body": "[[body]]"
  }
}
```
`body.json`:
```json
{
  "msg" : "[[msg]]"
}
```
`default.properties`:
```properties
msg=internal-server-error
```

**`create-booking/default/`**:

`mock.json`:
```json
{
  "request": {
    "method": "POST",
    "urlPath": "/booking"
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
`body.json`:
```json
{
  "bookingid": 1234,
  "booking": {
    "firstname": "John",
    "lastname": "Doe",
    "totalprice": 150,
    "depositpaid": true,
    "bookingdates": {
      "checkin": "2025-01-01",
      "checkout": "2025-01-10"
    },
    "additionalneeds": "Breakfast"
  }
}
```
`default.properties`: **empty file** (zero bytes / no content) — this template has no `[[token]]` placeholders in its `body.json`, so no properties are needed, but the (empty) file must still exist because `MockFactory.loadAndMergeProperties` unconditionally calls `props.load(...)` on it.

### 7.10 `src/test/resources/serenity.conf`

```hocon
environments{

    default{
        app.baseURL = "http://localhost:8081"
        wiremock_host = "localhost"
        wiremock_port = "9090"
        wiremock_protocol = "http"
    }

}
```

HOCON format, read by Serenity's `EnvironmentSpecificConfiguration`. Additional named environments (e.g. `staging { ... }`) can be added as sibling blocks under `environments{}` and selected via `-Denvironment=staging`.

**Configuration note to preserve as-is:** the `default` environment here points `app.baseURL` at `localhost:8081` while `wiremock_port` is `9090` — i.e., the fictitious "app under test" and the WireMock stub-management port are configured as two different addresses. Since `MockServerFactory` never starts a local server (see §6.1), running this demo suite for real requires an already-running WireMock instance reachable at the configured `wiremock_host:wiremock_port` **and** the "app" endpoints in `TestConstants` (`/v1/booking/get/{bookingId}`, `/v1/booking/create`) must be exposed on `app.baseURL`. In practice, for the demo suite's HTTP calls (`BookingAPIClient`) to actually reach the WireMock stubs registered via `MockFactory`, an operator must either (a) run a real standalone WireMock server on `localhost:8081` and pass `-Dwiremock_host=localhost -Dwiremock_port=8081 -Dwiremock_protocol=http` to align stub registration with where the client sends requests, or (b) adjust `serenity.conf`/system properties so `app.baseURL` and `wiremock_host:wiremock_port` refer to the same running instance. Reproduce the config values exactly as given above; do not "fix" this mismatch unless explicitly asked to.

### 7.11 `src/test/resources/junit-platform.properties`

```properties
cucumber.execution.parallel.enabled=false
cucumber.execution.parallel.config.strategy=fixed
cucumber.execution.parallel.config.fixed.parallelism=1
cucumber.execution.parallel.config.fixed.max-pool-size=1
```

### 7.12 `src/test/resources/logback-test.xml`

```xml
<configuration>
  <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
    <layout class="ch.qos.logback.classic.PatternLayout">
      <Pattern>
        %d{yyyy-MM-dd HH:mm:ss.SSS} %-5level %logger{36} - %msg%replace(%xException){'\n', ' '}%nopex%n
      </Pattern>
    </layout>
  </appender>

  <logger name="com.wiremock.utility.test" level="INFO"/>
  <root level="WARN">
    <appender-ref ref="STDOUT"/>
  </root>
</configuration>
```

Overrides the main `logback.xml` when tests run (test-classpath resource takes precedence), keeping test console output focused (`com.wiremock.utility.test` at `INFO`, everything else at `WARN`).

---

## 8. Package/Class Naming Conventions to Preserve

| Concern | Convention |
|---|---|
| Library root package | `com.wiremock.utility` |
| Cucumber glue shipped by the library | `com.wiremock.utility.bdd` |
| Demo suite root package | `com.wiremock.utility.test` |
| Demo suite sub-packages | `client`, `hooks`, `model`, `runner`, `stepdef`, `steps`, `utils` |
| Lombok usage | `@Slf4j` for logging; `@Data @NoArgsConstructor @AllArgsConstructor @Builder` for DTOs (`BookingRequest`, `BookingDates`) |
| Serenity action-class pattern | Cucumber `@When/@Then/@And` glue (`BookingAPIStepDef`) delegates via `@Steps`-injected field to a plain step/action class (`BookingAPISteps`) whose methods are annotated `@Step` |
| Mock resource convention | `mocks/{service}/{apiMethod}/{template}/{mock.json,body.json,default.properties}`, `template` name `default` used when the feature step omits `using template "..."` |
| Token syntax | `[[tokenName]]` in both `mock.json` and `body.json`; reserved token `[[body]]` for the rendered/escaped body |

---

## 9. What the Library's Public API Looks Like to a Consumer

```java
// 1. Get / lazily-init the shared WireMock client
WireMock client = MockServerFactory.wireMock();
String baseUrl = MockServerFactory.getBaseUrl(); // e.g. "https://localhost:443" by default, or configured value

// 2. Register a stub straight from a template (no overrides)
MockFactory.createMock("booking-svc", "get-booking", "default", null);

// 3. Register a stub with runtime token overrides
MockFactory.createMock("booking-svc", "get-booking", "bad-request", Map.of("msg", "custom-message"));
```

```gherkin
# Cucumber usage once MockSetupGlueSteps' package is added to the runner's glue paths
Given "bank-fn-svc" service mock for api method "callback-account-info"
Given "bank-fn-svc" service mock for api method "callback-account-info" using template "inactive-account"
Given "bank-fn-svc" service mock for api method "callback-account-info" with data
  | accountId  | status   |
  | 1111111111 | INACTIVE |
Given "bank-fn-svc" service mock for api method "callback-account-info" using template "custom" with data
  | accountId  | status |
  | 3333333333 | CLOSED |
```

Consumer runner setup:
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

## 10. Commands

```bash
# Compile only
./gradlew compileJava        # or .\gradlew.bat compileJava on Windows

# Build the fat JAR (tests are skipped by design via the `onlyIf` guard)
./gradlew build
# → build/libs/wiremock-utility-1.0.0-all.jar

# Publish to local Maven repo
./gradlew publishToMavenLocal

# Run all BDD tests explicitly
./gradlew test

# Run with a Cucumber tag expression
./gradlew test -Dtags="@CreateBooking"
./gradlew test -Dtags="@smoke and not @wip"

# Run against a named serenity.conf environment
./gradlew test -Denvironment=staging

# Point at a remote/already-running WireMock instance
./gradlew test -Dwiremock_host=my-wiremock.example.com -Dwiremock_port=443 -Dwiremock_protocol=https
```

Serenity aggregate HTML report is generated at `target/site/serenity/index.html` after `gradle test` (via `finalizedBy('aggregate')`); execution timeline at `build/test-results/timeline/`; raw Cucumber JSON at `target/cucumber-reports/`.

---

## 11. Known Quirks / Deliberate "As-Is" Behaviours (reproduce exactly unless told to fix)

1. **`MockServerFactory` never starts a local `WireMockServer`** — the class and README both talk about auto-starting a server on `localhost:9081`, but that code path is effectively absent from the live implementation; `wireMock()` always builds a remote `WireMock` API client against whatever `wiremock_host`/`wiremock_port`/`wiremock_protocol` resolve to, defaulting to `https://localhost:443`. The `WireMockServer server` field and `defaultLocalPort` constant are unused/dead.
2. **`serenity.conf`'s `default` environment configures `app.baseURL=http://localhost:8081` and `wiremock_port=9090`** — two different ports for "the app" vs. "the mock server's stub-management API," even though in this demo suite the "app" endpoints are really just paths on the same WireMock instance the stubs are registered against. Running the demo suite for real requires reconciling these (see §7.10).
3. **`get-booking/default/` template returns HTTP 401**, not a generic "success" response — there is no `success` template folder for `get-booking` in this repo; only `default` (401), `bad-request` (400), `internal-server-error` (500).
4. **`create-booking/default/default.properties` is an empty file** (present but with no key/value lines) — required to exist because `MockFactory` always attempts to load it, but it contributes no tokens.
5. **WireMock verification in `BookingAPISteps.validateBookingPayload` checks path `/booking`** (matching the stub's own `urlPath`), independent of `TestConstants.ADD_BOOKING_ENDPOINT` (`/v1/booking/create`), which is the path the REST client actually calls.
6. **`jar.enabled = false`** — only the shaded/fat jar task (`fatJar`) is enabled; the default Gradle `jar` task is explicitly disabled.
7. **`gradle build` deliberately skips tests** via the `test.onlyIf { taskNames contains 'test' }` guard — a consumer regenerating this project must not "simplify away" this guard, since it is an explicit, documented design decision (fat-jar builds should be fast/non-flaky and not depend on a live WireMock instance).
8. **Cucumber `GLUE_PROPERTY_NAME` must be a single, comma-separated `@ConfigurationParameter`** — splitting it into multiple annotations on the same runner class causes JUnit Platform to keep only the last one (duplicate-key overwrite), silently dropping glue packages.

---

## 12. Documentation to Also Generate

Regenerate `README.md` with the same structure as this project's actual README (Requirements, Build, Project Structure, Architecture Overview diagram, Classes, Resource File Structure, System Properties, BDD Tests, BDD Usage in Consumer Projects, Programmatic Usage, Logging, Dependencies) — the full current README content can be treated as the canonical prose documentation to reproduce, keeping in mind the discrepancy noted in §11.1 (the README describes the *intended*/historical local-auto-start behaviour; decide with the requester whether the regenerated project should match the README's description or the current code's actual behaviour — this document (`PROJECT_CONTEXT.md`) describes the **actual current code**).
