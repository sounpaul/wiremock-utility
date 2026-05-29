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

    private MockFactory() {
    }

    public static void createMock(
            String service, String apiMethod, String template, Map<String, String> values) {
        String templatePath = "mocks/" + service + "/" + apiMethod + "/" + template + "/";
        String nonEscapedBodyStr = getStringFromFile(templatePath + "body.json");
        String bodyStr = new String(JsonStringEncoder.getInstance().quoteAsString(nonEscapedBodyStr));
        Properties props =
                loadAndMergeProperties(templatePath + "default.properties", values);
        String stubMappingStr = getStringFromFile(templatePath + "mock.json");
        stubMappingStr = stubMappingStr.replace("[[body]]", bodyStr);

        for (String propName : props.stringPropertyNames()) {
            stubMappingStr =
                    stubMappingStr.replace("[[" + propName + "]]", translate(props.getProperty(propName)));
        }

        MockServerFactory.wireMock().register(StubMapping.buildFrom(stubMappingStr));
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
            try (InputStream in =
                         ClassLoader.getSystemClassLoader().getResourceAsStream(correctFile)) {
                return IOUtils.toString(in, "utf-8");
            } catch (IOException e) {
                log.error("Error in reading file {}", f.getFile(), e);
                return "";
            }
        }
    }

    private static Properties loadAndMergeProperties(
            String defaultPropsFilePath, Map<String, String> values) {
        Properties props = new Properties();

        try {
            props.load(
                    ClassLoader.getSystemClassLoader().getResourceAsStream(defaultPropsFilePath));
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
