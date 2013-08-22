/* 
 * i18n-checker: https://github.com/w3c/i18n-checker
 *
 * Copyright © 2013 World Wide Web Consortium, (Massachusetts Institute of
 * Technology, European Research Consortium for Informatics and Mathematics,
 * Keio University, Beihang). All Rights Reserved. This work is distributed
 * under the W3C® Software License [1] in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * 
 * [1] http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231
 */
package org.w3.i18n;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Joseph J Short
 */
public class I18nTestRunner {

    static {
        // TODO: Parse a 'test.conf' file.
    }
    private static final String TEST_URL =
            "http://www.w3.org/International/tests/i18n-checker/generate";

    @Test
    public void testCharsetTests() {
        int testsFailed = runTestsForFile(new File(
                "target/test-classes/tests_charsets.properties"));
        if (testsFailed > 0) {
            fail("Failed " + testsFailed + " I18nTests generated from"
                    + " 'tests_charsets.properties'.");
        }
    }

    @Test
    public void testLanguageTests() {
        int testsFailed = runTestsForFile(new File(
                "target/test-classes/tests_language.properties"));
        if (testsFailed > 0) {
            fail("Failed " + testsFailed + " I18nTests generated from"
                    + " 'tests_language.properties'.");
        }
    }

    @Test
    public void testMarkupTests() {
        int testsFailed = runTestsForFile(new File(
                "target/test-classes/tests_markup.properties"));
        if (testsFailed > 0) {
            fail("Failed " + testsFailed + " I18nTests generated from"
                    + " 'tests_markup.properties'.");
        }
    }

    @Test
    public void testNonLatinTests() {
        int testsFailed = runTestsForFile(new File(
                "target/test-classes/tests_nonLatin.properties"));
        if (testsFailed > 0) {
            fail("Failed " + testsFailed + " I18nTests generated from"
                    + " 'tests_nonLatin.properties'.");
        }
    }

    private static int runTestsForFile(File file) {
        int testsFailed = 0;
        Map<I18nTest, DocumentResource> testsWithResources =
                prepareDocumentResources(parseTestsFile(file));
        for (Map.Entry<I18nTest, DocumentResource> entry
                : testsWithResources.entrySet()) {
            boolean passed = run(entry.getKey(), entry.getValue());
            if (!passed) {
                testsFailed++;
            }
        }
        System.out.println();
        return testsFailed;
    }

    /* Prepares a DocumentResource for each distinct URL in the list of
     * I18nTests and links them together in a map. */
    private static Map<I18nTest, DocumentResource> prepareDocumentResources(
            List<I18nTest> i18nTests) {
        System.out.println("Retrieving remote resources ...");

        Map<I18nTest, DocumentResource> results = new TreeMap<>();

        // Prepare a document resource for each distinct URL.
        Set<URL> urls = new HashSet<>();
        for (I18nTest i18nTest : i18nTests) {
            urls.add(i18nTest.getUrl());
        }
        Map<URL, DocumentResource> documentResources;
        try {
            documentResources = DocumentResource.getRemote(urls);
        } catch (IOException ex) {
            throw new RuntimeException(
                    "Problem retrieving remote documents for testing.", ex);
        }

        // Link each I18nTest to a document resource and return the result.
        for (I18nTest i18nTest : i18nTests) {
            results.put(i18nTest, documentResources.get(i18nTest.getUrl()));
        }
        return results;
    }

    private static List<I18nTest> parseTestsFile(File file) {
        System.out.println("Parsing tests file: '" + file + "'.");

        Configuration configuration;
        try {
            configuration = new PropertiesConfiguration(file);
        } catch (ConfigurationException ex) {
            throw new RuntimeException(ex);
        }

        // Find all the test definition prefixes (e.g. "charset_19d").
        Iterator<String> keys = configuration.getKeys();
        Set<String> testPrefixes = new TreeSet<>();
        Pattern prefixPattern = Pattern.compile("^[^;][^_]+_[^_]+");
        while (keys.hasNext()) {
            Matcher prefixMatcher = prefixPattern.matcher(keys.next());
            if (prefixMatcher.find()) {
                testPrefixes.add(prefixMatcher.group());
            }
        }

        // Create I18nTests for each prefix.
        List<I18nTest> i18nTests = new ArrayList<>();
        for (String prefix : testPrefixes) {
            try {
                List<I18nTest> testsForPrefix =
                        constructFrom(prefix, configuration);
                System.out.println("Created " + testsForPrefix.size()
                        + " I18nTest(s) for prefix \"" + prefix + "\" in '"
                        + file.getName() + "'.");
                i18nTests.addAll(testsForPrefix);
            } catch (TestsFileParsingException ex) {
                throw new RuntimeException(
                        "Could not parse '" + file + "'.", ex);
            }
        }
        System.out.println();
        return i18nTests;
    }

    /* This methods interprets a test definition with the given prefix in the
     * given Configuration. The test definition must give expected reports
     * ("_report[]= ..."). are */
    private static List<I18nTest> constructFrom(
            String prefix, Configuration configuration)
            throws TestsFileParsingException {
        List<I18nTest> i18nTests = new ArrayList<>();

        // Retrieve details from the properties files.
        String propertyDescription = configuration.containsKey(prefix)
                ? configuration.getString(prefix).replace("\"", "")
                : null;
        String propertyId = configuration.containsKey(prefix + "_id")
                ? configuration.getString(prefix + "_id").replace("\"", "")
                : null;
        String propertyUrl = configuration.containsKey(prefix + "_url")
                ? configuration.getString(prefix + "_url").replace("\"", "")
                : null;
        String[] propertyTestFor =
                configuration.containsKey(prefix + "_test_for")
                ? configuration.getStringArray(prefix + "_test_for") : null;
        String[] propertyReport =
                configuration.containsKey(prefix + "_report[]")
                ? configuration.getStringArray(prefix + "_report[]") : null;
        if (propertyReport == null) {
            String singlePropertyReport =
                    configuration.containsKey(prefix + "_report")
                    ? configuration.getString(prefix + "_report") : null;
            if (singlePropertyReport != null) {
                propertyReport = new String[]{singlePropertyReport};
            }
        }

        // Check that vital details are set ('_testFor' and '_report[]').
        boolean useableTest =
                propertyTestFor != null && propertyTestFor.length != 0
                && propertyReport != null && propertyReport.length != 0;
        // Check that there's at least one non-empty report definition.
        if (useableTest) {
            useableTest = true;
            int i = 0;
            while (i < propertyReport.length) {
                propertyReport[i] = propertyReport[i].replace("\"", "");
                if (propertyReport[i].isEmpty()) {
                    useableTest = false;
                }
                i++;
            }
        }

        if (useableTest) {

            // Validate the given URL if present.
            URL givenUrl = null;
            if (propertyUrl != null) {
                try {
                    givenUrl = new URL(propertyUrl);
                } catch (MalformedURLException ex) {
                    throw new TestsFileParsingException("Invalid URL given"
                            + " by property. Propety name: \"" + prefix
                            + "_url\", extracted URL: " + propertyUrl + ".",
                            ex);
                }
            }

            // Create a list of assertions from the expected reports.
            List<Assertion> expectedAssertions = new ArrayList<>();
            for (String report : propertyReport) {
                if (!report.isEmpty()) {
                    report = report.replace("}", "");
                    String[] reportSplit = report.replace("}", "").split("\\{");
                    String reportId = reportSplit[0];
                    Assertion.Level level = Assertion.Level.INFO;

                    /* Look for additional details for each '_report[]'
                     * (e.g. "{severity:warning,tags:2}"). */
                    if (reportSplit.length != 1) {
                        String[] details = reportSplit[1].split(":");
                        for (String detail : details) {
                            switch (detail.trim()) {
                                case "severity:info":
                                    level = Assertion.Level.INFO;
                                    break;
                                case "severity:warning":
                                    level = Assertion.Level.WARNING;
                                    break;
                                case "severity:error":
                                    level = Assertion.Level.ERROR;
                                    break;
                            }
                        }
                    }
                    expectedAssertions.add(new Assertion(
                            reportId, level, "", "", new ArrayList<String>()));
                }
            }
            if (expectedAssertions.isEmpty()) {
                throw new TestsFileParsingException(
                        "Could not create any Assertions from  the reports"
                        + " property for \"" + prefix + "\". Interpreted"
                        + " reports property: "
                        + Arrays.toString(propertyReport) + ".");
            }
            Collections.sort(expectedAssertions);

            // Prepare test URLs and create I18nTests.
            for (String testFor : propertyTestFor) {
                testFor = testFor.replace("\"", "");
                String[] testForSplit = testFor.split(":");
                String format = testForSplit[0];
                String serveAs = testForSplit.length != 1
                        ? testForSplit[1] : "html";
                URL testUrl;
                if (givenUrl == null) {
                    try {
                        testUrl = constructTestUrl(propertyId, format, serveAs);
                    } catch (MalformedURLException ex) {
                        throw new TestsFileParsingException("Could not"
                                + " construct a URL from testFor property."
                                + " <TEST_URL>: " + TEST_URL + ", propety name:"
                                + " \"" + prefix + "_test_for\".", ex);
                    }
                } else {
                    testUrl = givenUrl;
                }
                i18nTests.add(new I18nTest(
                        prefix, propertyDescription, propertyId, testUrl,
                        format, serveAs, expectedAssertions));
            }
        }
        return i18nTests;
    }

    private static URL constructTestUrl(
            String id, String format, String serveAs)
            throws MalformedURLException {
        if (id == null || format == null || serveAs == null) {
            throw new NullPointerException();
        }
        return new URL(TEST_URL + "?test=" + id + "&format=" + format
                + "&serveas=" + serveAs);
    }

    /* Returns true if the test succeeds, others false. Prints details of the
     * test to System.out. */
    private static boolean run(
            I18nTest i18nTest, DocumentResource documentResource) {
        boolean passed;
        System.out.println("\nRunning test: " + i18nTest + ".");

        // Generate a list of assertions using the checker.
        List<Assertion> generatedAssertions =
                new Check(documentResource).getAssertions();

        // Compare the lists of assertions.
        System.out.println(
                "Expected assertions: " + i18nTest.getExpectedAssertions());
        int expectedAssertionsFound = 0;
        for (Assertion assertion : i18nTest.getExpectedAssertions()) {
            boolean found = false;
            int i = generatedAssertions.size();
            while (found == false && i < generatedAssertions.size()) {
                // Currently compares only by id and level.
                if (assertion.getId().equals(
                        generatedAssertions.get(i).getId())
                        && assertion.getLevel().equals(
                        generatedAssertions.get(i).getLevel())) {
                    found = true;
                    expectedAssertionsFound++;
                }
                i++;
            }
            if (generatedAssertions.contains(assertion)) {
                expectedAssertionsFound++;
            }
        }
        System.out.println("Generated assertions: " + generatedAssertions + ".");

        // Determine result.
        passed = expectedAssertionsFound
                == i18nTest.getExpectedAssertions().size();
        System.out.println("Result: " + (passed ? "Passed" : "FAILED")
                + " (found " + expectedAssertionsFound + " of "
                + i18nTest.getExpectedAssertions().size() + " expected, "
                + generatedAssertions.size() + " generated.)");
        return passed;
    }

    public static class TestsFileParsingException extends Exception {

        public TestsFileParsingException() {
        }

        public TestsFileParsingException(String message) {
            super(message);
        }

        public TestsFileParsingException(Throwable cause) {
            super(cause);
        }

        public TestsFileParsingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
