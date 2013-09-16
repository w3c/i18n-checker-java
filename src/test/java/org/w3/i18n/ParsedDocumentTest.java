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
public class ParsedDocumentTest {

    private static ParsedDocument instance1;
    private static ParsedDocument instance2;

    public ParsedDocumentTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        try {
            // Define instance1 (ParsedDocument).
            URL url1 = new URL("http://www.w3.org/");
            InputStream body1 = new FileInputStream(
                    "src/test/resources/www.w3.org.htm");
            Map<String, List<String>> headers1 = new HashMap<>();
            headers1.put("Content-Type",
                    Arrays.asList("text/html; charset=utf-8"));
            instance1 = new ParsedDocument(
                    new DocumentResource(url1, body1, headers1));

            // Define instance2 (ParsedDocument).
            URL url2 = new URL("http://www.chinaw3c.org/");
            InputStream body2 = new FileInputStream(
                    "src/test/resources/www.chinaw3c.org.htm");
            Map<String, List<String>> headers2 = new HashMap<>();
            headers2.put("Content-Type",
                    Arrays.asList("text/html; charset=UTF-8"));
            instance2 = new ParsedDocument(
                    new DocumentResource(url2, body2, headers2));

        } catch (FileNotFoundException | MalformedURLException ex) {
            throw new RuntimeException(
                    "Test resources defined incorrectly.", ex);
        }
    }

    @Test
    public void testGetDoctypeDeclaration() {
        System.out.println("Testing: getDoctypeDeclaration");
        assertEquals(
                "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\""
                + " \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">",
                instance1.getDoctypeTag());
        assertEquals("<!DOCTYPE html>", instance2.getDoctypeTag());
    }

    @Test
    public void testIsHtml() {
        System.out.println("Testing: isHtml");
        assertFalse(instance1.isHtml());
        assertFalse(instance2.isHtml());
    }

    @Test
    public void testIsHtml5() {
        System.out.println("Testing: isHtml5");
        assertFalse(instance1.isHtml5());
        assertTrue(instance2.isHtml5());
    }

    @Test
    public void testIsXhtml10() {
        System.out.println("Testing: isXhtml10");
        assertTrue(instance1.isXhtml10());
        assertFalse(instance2.isXhtml10());
    }

    @Test
    public void testIsXhtml11() {
        System.out.println("Testing: isXhtml11");
        assertFalse(instance1.isXhtml11());
        assertFalse(instance2.isXhtml11());
    }

    @Test
    public void testIsXhtml1X() {
        System.out.println("Testing: isXhtml1X");
        assertTrue(instance1.isXhtml1X());
        assertFalse(instance2.isXhtml1X());
    }

    @Test
    public void testIsRdfA() {
        System.out.println("Testing: isRdfA");
        assertFalse(instance1.isRdfA());
        assertFalse(instance2.isRdfA());
    }

    @Test
    public void testGetByteOrderMark() {
        System.out.println("Testing: getByteOrderMark");
        assertNull(instance1.getByteOrderMark());
        assertNull(instance2.getByteOrderMark());
    }

    @Test
    public void testGetXmlDeclaration() {
        System.out.println("Testing: getXmlDeclaration");
        assertNull(instance1.getXmlDeclaration());
        assertNull(instance2.getXmlDeclaration());
    }

    @Test
    public void testGetOpeningHtmlTag() {
        System.out.println("Testing: getOpeningHtmlTag");
        assertEquals(instance1.getOpeningHtmlTag(),
                "<html xmlns=\"http://www.w3.org/1999/xhtml\""
                + " xml:lang=\"en\" lang=\"en\">");
        assertEquals(instance2.getOpeningHtmlTag(),
                "<html lang=\"zh\">");
    }

    @Test
    public void testGetCharsetXmlDeclaration() {
        System.out.println("Testing: getCharsetXmlDeclaration");
        assertNull(instance1.getCharsetXmlDeclaration());
        assertNull(instance2.getCharsetXmlDeclaration());
    }

    @Test
    public void testGetCharsetHttp() {
        System.out.println("Testing: getCharsetHttp");
        assertEquals(instance1.getCharsetHttp(), "utf-8");
        assertEquals(instance2.getCharsetHttp(), "UTF-8");
    }

    @Test
    public void testGetContentType() {
        System.out.println("Testing: getContentType");
        assertEquals(
                instance1.getContentType(), "text/html; charset=utf-8");
        assertEquals(
                instance2.getContentType(), "text/html; charset=UTF-8");
    }

    @Test
    public void testIsServedAsXml() {
        System.out.println("Testing: isServedAsXml");
        assertFalse(instance1.isServedAsXml());
        assertFalse(instance2.isServedAsXml());
    }
}