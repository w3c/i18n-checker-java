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
import java.util.Collection;
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
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Joseph J Short
 */
@RunWith(Parameterized.class)
public class I18nTestRunnerTest {

    private I18nTest i18nTest;
    private DocumentResource documentResource;
    private static final Logger logger =
            LoggerFactory.getLogger(I18nTestRunnerTest.class);

    public I18nTestRunnerTest(
            I18nTest i18nTest, DocumentResource documentResource) {
        this.i18nTest = i18nTest;
        this.documentResource = documentResource;
    }

    @Parameters
    public static Collection<Object[]> prepareParameterValues() {
        List<Object[]> parameterValues = new ArrayList<>();
        Map<I18nTest, DocumentResource> testsWithResources = new TreeMap<>();
        for (File file : new File[]{
            new File("target/test-classes/tests_charsets.properties"),
            new File("target/test-classes/tests_language.properties"),
            new File("target/test-classes/tests_markup.properties"),
            new File("target/test-classes/tests_nonLatin.properties")}) {
            testsWithResources.putAll(
                    prepareDocumentResources(parseTestsFile(file)));
        }
        for (Map.Entry<I18nTest, DocumentResource> entry
                : testsWithResources.entrySet()) {
            parameterValues.add(
                    new Object[]{entry.getKey(), entry.getValue()});
        }
        return parameterValues;
    }

    private static List<I18nTest> parseTestsFile(File file) {
        logger.info("Parsing tests file: '" + file + "'.");

        // Parse the properties file.
        PropertiesConfiguration configuration = new PropertiesConfiguration();
        // (This method must be called before the file is parsed).
        configuration.setDelimiterParsingDisabled(true);
        try {
            configuration.load(file); // (Parses.)
        } catch (ConfigurationException ex) {
            throw new RuntimeException(ex);
        }

        // Create I18nTests for each prefix (e.g. "charset_5ab") in the file.
        Set<String> prefixes = findPrefixes(configuration);
        List<I18nTest> i18nTests = new ArrayList<>();
        for (String prefix : prefixes) {
            List<I18nTest> testsForPrefix =
                    interpretTests(prefix, configuration);
            logger.info("Created " + testsForPrefix.size()
                    + " I18nTest(s) for prefix '" + prefix + "' in '"
                    + file.getName() + "'.");
            i18nTests.addAll(testsForPrefix);
        }
        Collections.sort(i18nTests);
        return i18nTests;
    }

    // Finds each key prefix (e.g. "charset_5ab") in the confugration.
    private static Set<String> findPrefixes(Configuration configuration) {
        Set<String> prefixes = new TreeSet<>();
        Pattern prefixPattern = Pattern.compile("^[^;][^_]+_[^_]+");

        // Iterate through all keys in the configuration.
        Iterator<String> keyIterator = configuration.getKeys();
        while (keyIterator.hasNext()) {

            // Add the prefix of each key to the set.
            String key = keyIterator.next();
            Matcher prefixMatcher = prefixPattern.matcher(key);
            if (prefixMatcher.find()) {
                prefixes.add(prefixMatcher.group());
            }
        }
        return prefixes;
    }

    /* This methods interprets a test definition with the given prefix in the
     * given Configuration. The test definition must give expected reports
     * (e.g. "_report[]= ..."). */
    private static List<I18nTest> interpretTests(
            String prefix, PropertiesConfiguration configuration)
            throws TestsFileParsingException {
        List<I18nTest> i18nTests = new ArrayList<>();

        /* Possible properties for a test:
         * (name)
         * 'id'
         * 'url'
         * 'test_for'
         * 'info_charset'
         * 'info_lang'
         * 'info_dir'
         * 'info_classId'
         * 'info_headers'
         * 'reports'
         * 'warning'
         * 'applicableOnlyTo'
         */

        // Retrieve required details from the properties files.
        String propertyDescription = configuration.containsKey(prefix)
                ? configuration.getString(prefix).replace("\"", "")
                : null;
        String propertyId = configuration.containsKey(prefix + "_id")
                ? configuration.getString(prefix + "_id").replace("\"", "")
                : null;
        String propertyUrl = configuration.containsKey(prefix + "_url")
                ? configuration.getString(prefix + "_url").replace("\"", "")
                : null;
        String propertyTestFor =
                configuration.containsKey(prefix + "_test_for")
                ? configuration.getString(prefix + "_test_for") : null;
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
                propertyTestFor != null && !propertyTestFor.isEmpty()
                && propertyReport != null && propertyReport.length != 0;
        // Check that there's at least one non-empty report definition.
        if (useableTest) {
            propertyTestFor = propertyTestFor.replaceAll("\"", "");
            if (propertyTestFor.isEmpty()) {
                useableTest = false;
            }
        }
        if (useableTest) {
            // Check that there's at least one non-empty report definition.
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
                            + "_url\", extracted URL: " + propertyUrl
                            + ", file: '" + configuration.getFileName() + "'.",
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

                    // MESSAGE levels are not compared.
                    Assertion.Level level = Assertion.Level.MESSAGE;

                    /* Look for additional details for each '_report[]'
                     * (e.g. "{severity:warning,tags:2}"). */
                    if (reportSplit.length != 1) {
                        String[] details = reportSplit[1].split(",");
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
                        + Arrays.toString(propertyReport) + ", file: '"
                        + configuration.getFileName() + "'.");
            }
            Collections.sort(expectedAssertions);

            // Prepare URLs and create I18nTests for each format and servaAs.
            for (String testFor : propertyTestFor.split(",")) {
                testFor = testFor.replace("\"", "");
                String[] testForSplit = testFor.split(":");
                String format = testForSplit[0];
                String serveAs = testForSplit.length != 1
                        ? testForSplit[1] : "html";
                URL testUrl;
                if (givenUrl == null) {
                    try {
                        testUrl = assembleTestUrl(propertyId, format, serveAs);
                    } catch (MalformedURLException ex) {
                        throw new TestsFileParsingException("Could not"
                                + " construct a URL from testFor property."
                                + " TestConfig.TEST_RUNNER_URL: "
                                + TestConfig.TEST_RUNNER_URL + ", propety name:"
                                + " \"" + prefix + "_test_for\", file: '"
                                + configuration.getFileName() + "'.", ex);
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

    private static URL assembleTestUrl(
            String id, String format, String serveAs)
            throws MalformedURLException {
        if (id == null || format == null || serveAs == null) {
            throw new NullPointerException();
        }
        return new URL(TestConfig.TEST_RUNNER_URL + "?test=" + id + "&format="
                + format + "&serveas=" + serveAs);
    }

    /* Prepares a DocumentResource for each distinct URL in the list of
     * I18nTests and links them together in a map. (This takes advantage of
     * "DocumentResource.getRemote(Set<URL> urls)".) */
    private static Map<I18nTest, DocumentResource> prepareDocumentResources(
            List<I18nTest> i18nTests) {

        Map<I18nTest, DocumentResource> testsWithResources = new TreeMap<>();

        // Prepare a document resource for each distinct URL.
        Set<URL> urls = new HashSet<>();
        for (I18nTest i18nTest : i18nTests) {
            urls.add(i18nTest.getUrl());
        }
        logger.info("Retrieving " + urls.size() + " remote resource(s).");
        Map<URL, DocumentResource> documentResources;
        try {
            documentResources = DocumentResource.getRemote(urls);
        } catch (IOException ex) {
            throw new RuntimeException(
                    "Problem retrieving remote documents for testing.", ex);
        }

        // Link each I18nTest to a document resource and return the result.
        for (I18nTest i18nTest : i18nTests) {
            testsWithResources.put(
                    i18nTest, documentResources.get(i18nTest.getUrl()));
        }
        return testsWithResources;
    }

    @Test
    public void run() {
        boolean passed;
        StringBuilder testSB = new StringBuilder();

        testSB.append("\nRunning test: '").append(i18nTest.getName())
                .append("' [").append(i18nTest.getUrl()).append("].");

        List<Assertion> expectedAssertions = i18nTest.getExpectedAssertions();

        // Generate a list of assertions using the checker.
        List<Assertion> generatedAssertions =
                new Check(new ParsedDocument(documentResource))
                .getAssertions();
        Collections.sort(generatedAssertions);

        // Search for and print the expected assertions.
        int expectedAssertionsFound = 0;
        StringBuilder expectedSB = new StringBuilder("Expected: [");
        for (Assertion expectedAssertion : expectedAssertions) {
            boolean found = false;
            int i = 0;
            while (!found && i < generatedAssertions.size()) {
                if (matches(generatedAssertions.get(i), expectedAssertion)) {
                    found = true;
                }
                i++;
            }
            if (found) {
                expectedAssertionsFound++;
            }
            expectedSB.append("[")
                    .append(expectedAssertion.getId())
                    .append(expectedAssertion.getLevel()
                    == Assertion.Level.MESSAGE
                    ? "" : ", " + expectedAssertion.getLevel())
                    .append("] (")
                    .append(found ? "found" : "MISSING")
                    .append("), ");
        }
        if (!expectedAssertions.isEmpty()) {
            expectedSB.replace(
                    expectedSB.length() - 2, expectedSB.length() - 1, "]");
        } else {
            expectedSB.append("]");
        }
        testSB.append("\n").append(expectedSB);

        // Sort generated assertions in to 3 categories and print.
        // (Expected reports.)
        List<Assertion> gFound = new ArrayList<>();
        // (Unexpected reports.)
        List<Assertion> gRepUnexpected = new ArrayList<>();
        // (Other.)
        List<Assertion> gOther = new ArrayList<>();
        for (Assertion generatedAssertion : generatedAssertions) {
            boolean expected = false;
            int i = 0;
            while (!expected && i < expectedAssertions.size()) {
                if (matches(generatedAssertion, expectedAssertions.get(i))) {
                    expected = true;
                }
                i++;
            }
            if (expected) {
                gFound.add(generatedAssertion);
            } else if (generatedAssertion.getId().matches("rep_.*")) {
                gRepUnexpected.add(generatedAssertion);
            } else {
                gOther.add(generatedAssertion);
            }
        }

        testSB.append("\nFound: ").append(toString(gFound)).append(
                "\nUnexpected 'rep': ").append(toString(gRepUnexpected))
                .append("\nOther: ").append(toString(gOther));

        // Determine result.
        passed = expectedAssertionsFound
                == i18nTest.getExpectedAssertions().size();

        testSB.append("\nResult: ").append(passed ? "Passed" : "FAILED")
                .append(" (generated: ").append(expectedAssertionsFound)
                .append(" of ").append(expectedAssertions.size())
                .append(" expected, ").append(gRepUnexpected.size())
                .append(" unexpected 'rep', and ").append(gOther.size())
                .append(" other).\n");
        if (passed) {
            logger.info(testSB.toString());
        } else {
            fail(testSB.toString());
        }
    }

    private static boolean matches(Assertion found, Assertion expected) {
        return found.getId().equals(expected.getId())
                && (expected.getLevel() == Assertion.Level.MESSAGE
                || found.getLevel() == expected.getLevel());
    }

    private static String toString(List<Assertion> assertions) {
        // Will look like: "[[charset_meta, INFO, [context]], ... ]".
        StringBuilder sb = new StringBuilder("[");
        for (Assertion assertion : assertions) {
            sb.append("[")
                    .append(assertion.getId()).append(", ")
                    .append(assertion.getLevel()).append(", ")
                    .append(assertion.getContexts()).append("], ");
        }
        if (!assertions.isEmpty()) {
            sb.replace(sb.length() - 2, sb.length() - 1, "]");
        } else {
            sb.append("]");
        }
        return sb.toString();
    }

    public static class TestsFileParsingException extends RuntimeException {

        static final long serialVersionUID = -1997103118729166042L;

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
