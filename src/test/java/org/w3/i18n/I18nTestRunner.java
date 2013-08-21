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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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

/**
 *
 * @author Joseph J Short
 */
@RunWith(Parameterized.class)
public class I18nTestRunner {

    static {
        // TODO: Parse a 'test.conf' file.
    }
    private static final String TEST_URL =
            "http://www.w3.org/International/tests/i18n-checker/generate";
    // The instance used by JUnit '@Parameters'.
    private I18nTest i18nTest;

    public I18nTestRunner(I18nTest i18nTest) {
        this.i18nTest = i18nTest;
    }

    // Creates I18nTest instances to be used as JUnit '@Parameters'.
    @Parameters
    public static Collection<Object[]> data() {
        Object[] testsCharset = parseTestsFile(
                new File("target/test-classes/tests_charsets.properties"))
                .toArray();
        Object[] testsLanguage = parseTestsFile(
                new File("target/test-classes/tests_language.properties"))
                .toArray();
        Object[] testsMarkup = parseTestsFile(
                new File("target/test-classes/tests_markup.properties"))
                .toArray();
        Object[] testsNonLatin = parseTestsFile(
                new File("target/test-classes/tests_nonLatin.properties"))
                .toArray();
        Collection<Object[]> data = new ArrayList<>();
        for (Object object : testsCharset) {
            data.add(new Object[]{object});
        }
        for (Object object : testsLanguage) {
            data.add(new Object[]{object});
        }
        for (Object object : testsMarkup) {
            data.add(new Object[]{object});
        }
        for (Object object : testsNonLatin) {
            data.add(new Object[]{object});
        }
        return data;
    }

    // The JUnit test that uses the '@Parameters' instance.
    @Test
    public void run() {
        System.out.println("\nRunning test: " + i18nTest);
        if (i18nTest.getUrl() != null) {
            List<Assertion> generatedAssertions;
            try {
                generatedAssertions =
                        I18nChecker.check(new URL(i18nTest.getUrl()));
            } catch (IOException ex) {
                throw new RuntimeException(
                        "Could not retrieve remote test document ("
                        + i18nTest.getUrl() + ")", ex);
            }
            System.out.print("Expected assertions: [");
            int expectedAssertionCount = 0;
            for (Assertion assertion : i18nTest.getExpectedAssertions()) {
                System.out.print(assertion.getId() + ", ");
                if (generatedAssertions.contains(assertion)) {
                    expectedAssertionCount++;
                }
            }
            System.out.println("].");
            System.out.print("Generated assertions: [");
            for (Assertion assertion : generatedAssertions) {
                System.out.print(assertion.getId() + ", ");
            }
            System.out.println("].");
            System.out.println("expectedAssertionCount = "
                    + expectedAssertionCount + " out of "
                    + i18nTest.getExpectedAssertions().size() + ".");
            if (expectedAssertionCount
                    != i18nTest.getExpectedAssertions().size()) {
                // TODO: This doesn't seem to work.
                fail("Only " + expectedAssertionCount + " out of "
                        + i18nTest.getExpectedAssertions().size()
                        + " expected assertions were generated.");
            }
        }
    }

    public static List<I18nTest> parseTestsFile(File file) {
        Configuration configuration;
        try {
            configuration = new PropertiesConfiguration(file);
        } catch (ConfigurationException ex) {
            throw new RuntimeException(ex);
        }
        Iterator<String> keys = configuration.getKeys();
        Set<String> testNames = new TreeSet<>();
        while (keys.hasNext()) {
            Matcher m = Pattern.compile("[^;][^_]*_[^_]*").matcher(keys.next());
            if (m.find()) {
                testNames.add(m.group());
            }
        }
        List<I18nTest> i18nTests = new ArrayList<>();

        for (String name : testNames) {
            // Retrieve keys as they appear in the properties files.
            String id = configuration.containsKey(name + "_id")
                    ? configuration.getString(name + "_id").replace("\"", "")
                    : null;
            String url = configuration.containsKey(name + "_url")
                    ? configuration.getString(name + "_url").replace("\"", "")
                    : null;
            String testFors = configuration.containsKey(name + "_test_for")
                    ? configuration.getString(name + "_test_for")
                    .replace("\"", "")
                    : null;
            String[] reports = configuration.containsKey(name + "_report[]")
                    ? configuration.getStringArray(name + "_report[]") : null;

            // Construct the new I18nTests from the details.
            List<Assertion> expectedAssertions = new ArrayList<>();
            if (reports != null) {
                for (String report : reports) {
                    report = report.replace("\"", "");
                    String reportId = report.split("\\{")[0];
                    expectedAssertions.add(
                            new Assertion(reportId, null, null, null, null));
                }
            }
            if (testFors != null) {
                String[] testForsArray = testFors.split(",");
                for (String testFor : testForsArray) {
                    testFor = testFor.replace("\"", "");
                    String[] testForSplit = testFor.split(":");
                    String format = testForSplit[0];
                    String serveAs = testForSplit.length != 1
                            ? testForSplit[1] : "html";
                    String genUrl = url == null && id != null & format != null
                            & serveAs != null
                            ? constructTestUrl(id, format, serveAs) : url;
                    i18nTests.add(new I18nTest(
                            name, id, genUrl, format, serveAs,
                            expectedAssertions));
                }
            }
        }
        return i18nTests;
    }

    private static String constructTestUrl(
            String id, String format, String serveAs) {
        if (id == null || format == null || serveAs == null) {
            throw new NullPointerException();
        }
        return TEST_URL + "?test=" + id + "&format=" + format
                + "&serveas=" + serveAs;
    }
//    @Test
//    public void testCharsetTests() {
//        List<I18nTest> i18nTests = parseTestsFile(
//                new File("target/test-classes/tests_charsets.properties"));
//        for (I18nTest i18nTest : i18nTests) {
//            run(i18nTest);
//            System.out.println();
//        }
//    }
//    @Test
//    public void testLanguageTests() {
//        List<I18nTest> i18nTests = parseTestsFile(
//                new File("target/test-classes/tests_languages.properties"));
//        for (I18nTest i18nTest : i18nTests) {
//            run(i18nTest);
//            System.out.println();
//        }
//    }
//    @Test
//    public void testMarkupTests() {
//        List<I18nTest> i18nTests = parseTestsFile(
//                new File("target/test-classes/tests_markup.properties"));
//        for (I18nTest i18nTest : i18nTests) {
//            run(i18nTest);
//            System.out.println();
//        }
//    }
//    @Test
//    public void testNonLatinTests() {
//        List<I18nTest> i18nTests = parseTestsFile(
//                new File("target/test-classes/tests_nonLatin.properties"));
//        for (I18nTest i18nTest : i18nTests) {
//            run(i18nTest);
//            System.out.println();
//        }
//    }
}
