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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Joseph J Short
 */
public class CheckTest {

    private static Check instance1;
    private static Check instance2;

    public CheckTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        try {
            // Define instance1 (Check).
            URL url1 = new URL("http://www.w3.org/");
            InputStream body1 = new FileInputStream(
                    "src/test/resources/www.w3.org.htm");
            Map<String, List<String>> headers1 = new HashMap<>();
            headers1.put("Content-Type",
                    Arrays.asList("text/html; charset=utf-8"));
            instance1 = new Check(
                    new ParsedDocument(
                    new DocumentResource(url1, body1, headers1)));

            // Define instance2 (Check).
            URL url2 = new URL("http://www.chinaw3c.org/");
            InputStream body2 = new FileInputStream(
                    "src/test/resources/www.chinaw3c.org.htm");
            Map<String, List<String>> headers2 = new HashMap<>();
            headers2.put("Content-Type",
                    Arrays.asList("text/html; charset=UTF-8"));

            instance2 = new Check(
                    new ParsedDocument(
                    new DocumentResource(url2, body2, headers2)));

        } catch (FileNotFoundException | MalformedURLException ex) {
            throw new RuntimeException(
                    "Test resources defined incorrectly.", ex);
        }
    }

    @Test
    public void testGetParsedDocument() {
        System.out.println("Testing: getParsedDocument");
        assertNotNull(instance1.getParsedDocument());
        assertNotNull(instance2.getParsedDocument());
    }

    @Test
    public void testGetAssertions() {
        System.out.println("Testing: getAssertions");
        assertNotNull(instance1.getParsedDocument());
        assertNotNull(instance2.getParsedDocument());
        assertFalse("No assertions were made by the check (instance1).",
                instance1.getAssertions().isEmpty());
        assertFalse("No assertions were made by the check (instance2).",
                instance2.getAssertions().isEmpty());
    }
}