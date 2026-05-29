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

            if ("localhost".equalsIgnoreCase(mockServerHost)) {
                WireMockConfiguration config = WireMockConfiguration.wireMockConfig().port(9081);
                server = new WireMockServer(config);

                try {
                    server.start();
                    log.info("WireMock server started on port {}", defaultLocalPort);
                } catch (RuntimeException ex) {
                    if (!(ex instanceof FatalStartupException)) {
                        throw ex;
                    }
                    log.warn("WireMock server already running on port {}", defaultLocalPort);
                }

                wireMockClient = new WireMock(9081);
                mockServerPort = 9081;
                mockServerProtocol = "http";
            } else {
                log.info("Connecting to remote WireMock at {}://{}:{}", mockServerProtocol, mockServerHost, mockServerPort);
                wireMockClient = new WireMock(mockServerProtocol, mockServerHost, mockServerPort);
            }

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
