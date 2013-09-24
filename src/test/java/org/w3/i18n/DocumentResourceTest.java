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
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Joseph J Short
 */
public class DocumentResourceTest {

    private static DocumentResource onlineInstance;
    private static DocumentResource offlineInstance;

    public DocumentResourceTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        URL url;
        try {
            url = new URL("http://www.w3.org/");

            // Define online instance.
            onlineInstance =
                    DocumentResource.getRemote(url);

            // Define offline instance.
            InputStream offlineBody = new FileInputStream(
                    "src/test/resources/www.w3.org.htm");
            Map<String, List<String>> offlineHeaders = new HashMap<>();
            offlineHeaders.put("Content-Type",
                    Arrays.asList("text/html; charset=utf-8"));
            offlineInstance =
                    new DocumentResource(url, offlineBody, offlineHeaders);
        } catch (MalformedURLException | FileNotFoundException ex) {
            throw new RuntimeException(
                    "Test resources defined incorrectly.", ex);
        } catch (IOException ex) {
            System.err.println("Warning: Resource at http://www.w3.org/ could"
                    + " not be retrieved for testing (Message: \""
                    + ex.getMessage() + "\"), tests skipped.");
            onlineInstance = null;
        }
    }

    @Test
    public void testGetUrl() {
        System.out.println("Testing: getUrl");
        if (onlineInstance != null) {
            assertEquals(onlineInstance.getUrl().toExternalForm(),
                    "http://www.w3.org/");
        }
        assertEquals(offlineInstance.getUrl().toExternalForm(),
                "http://www.w3.org/");
    }

    @Test
    public void testGetHeaders() {
        System.out.println("Testing: getHeaders");
        if (onlineInstance != null) {
            assertNotNull(onlineInstance.getHeaders());
        }
        assertTrue(offlineInstance.getHeaders().containsKey("Content-Type"));
    }

    @Test
    public void testGetHeader() {
        System.out.println("Testing: getHeader");
        assertEquals(offlineInstance.getHeader("Content-Type"),
                "text/html; charset=utf-8");
    }

    @Test
    public void testGetBody() {
        System.out.println("Testing: getBody");
        if (onlineInstance != null) {
            assertNotNull(onlineInstance.getBody());
            try (Scanner scanner =
                    new Scanner(onlineInstance.getBody(), "UTF-8")) {
                scanner.useDelimiter("\\A");
                if (scanner.hasNext()) {
                    scanner.next();
                }
            }
        }
        assertNotNull(offlineInstance.getBody());
        try (Scanner scanner =
                new Scanner(offlineInstance.getBody(), "UTF-8")) {
            scanner.useDelimiter("\\A");
            if (scanner.hasNext()) {
                scanner.next();
            }
        }
    }
}