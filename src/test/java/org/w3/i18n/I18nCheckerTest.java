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

import com.ning.http.client.AsyncHttpClient;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3.assertor.model.Assertion;

/**
 *
 * @author Joseph J Short
 */
public class I18nCheckerTest {

    private static AsyncHttpClient asyncHttpClient;

    public I18nCheckerTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        asyncHttpClient = new AsyncHttpClient();
    }

    @AfterClass
    public static void tearDownClass() {
        asyncHttpClient.close();
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testAssertUri() {
        System.out.println("Information: Testing i18n-checker using"
                + " \"http://www.w3.org/\" as the URI.");
        URI uri = null;
        try {
            uri = new URI("http://www.w3.org/");
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
        Iterable<Assertion> assertions = new I18nChecker().assertUri(
                uri, asyncHttpClient, null);
        for (Assertion assertion : assertions) {
            System.out.println(assertion.getLevel()
                    + ": \"" + assertion.getId()
                    + "\"; \"" + assertion.getHtmlTitle()
                    + "\"; \"" + assertion.getHtmlDescription()
                    + "\"; " + assertion.getContexts());
        }
    }
}