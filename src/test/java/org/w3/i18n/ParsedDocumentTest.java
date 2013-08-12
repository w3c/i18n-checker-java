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

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Joseph J Short
 */
public class ParsedDocumentTest {

    private String doctypeDeclaration;
    private String byteOrderMark;
    private String xmlDeclaration;
    private String openingHtmlTag;
    private String charsetXmlDeclaration;
    private String charsetMeta;
    private String charsetMetaContext;
    private String contentType;
    private boolean servedAsXml;
    private String charsetHttp;
    private String htmlTitle;
    private String documentBody;
    private DocumentResource documentResource;
    private ParsedDocument instance;

    public ParsedDocumentTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {

        doctypeDeclaration = "<!DOCTYPE html PUBLIC \"-//W3C//DTD"
                + " XHTML 1.0 Strict//EN\" \"DTD/xhtml1-strict.dtd\">";
        byteOrderMark = null;
        charsetXmlDeclaration = "UTF-8";
        xmlDeclaration = "<?xml version=\"1.0\" encoding=\""
                + charsetXmlDeclaration + "\"?>";
        openingHtmlTag = "<html xmlns=\"http://www.w3.org/1999/xhtml\""
                + " xml:lang=\"en\" lang=\"en\">";
        charsetMeta = null;
        charsetMetaContext = null;
        charsetHttp = "utf-8";
        contentType = "text/html; charset=" + charsetHttp;
        servedAsXml = false;
        htmlTitle = "Testing.";
        String testBody = xmlDeclaration + doctypeDeclaration + openingHtmlTag
                + "<head><title>" + htmlTitle + "</title></head>"
                + "<body><h1>Test!</h1></body></html>";
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Content-Type", Arrays.asList(contentType));
        headers.put("Content-Language", Arrays.asList("en"));
        try {
            this.documentResource = new DocumentResource(
                    new URL("http://www.example.com"),
                    new ByteArrayInputStream(testBody.getBytes()),
                    headers);
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
        instance = new ParsedDocument(documentResource);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetDocument() {
        System.out.println("Testing: getDocument");
        assertEquals(htmlTitle, instance.getDocument().title());
    }

    @Test
    public void testGetDoctypeDeclaration() {
        System.out.println("Testing: getDoctypeDeclaration");
        assertEquals(doctypeDeclaration, instance.getDoctypeDeclaration());
    }

    @Test
    public void testIsHtml() {
        System.out.println("Testing: isHtml");
        assertFalse(instance.isHtml());
    }

    @Test
    public void testIsHtml5() {
        System.out.println("Testing: isHtml5");
        assertFalse(instance.isHtml5());
    }

    @Test
    public void testIsXhtml10() {
        System.out.println("Testing: isXhtml10");
        assertTrue(instance.isXhtml10());
    }

    @Test
    public void testIsXhtml11() {
        System.out.println("Testing: isXhtml11");
        assertFalse(instance.isXhtml11());
    }

    @Test
    public void testIsXhtml1X() {
        System.out.println("Testing: isXhtml1X");
        assertTrue(instance.isXhtml1X());
    }

    @Test
    public void testIsRdfA() {
        System.out.println("Testing: isRdfA");
        assertFalse(instance.isRdfA());
    }

    @Test
    public void testGetByteOrderMark() {
        System.out.println("Testing: getByteOrderMark");
        assertNull(instance.getByteOrderMark());
    }

    @Test
    public void testGetXmlDeclaration() {
        System.out.println("Testing: getXmlDeclaration");
        assertEquals(xmlDeclaration, instance.getXmlDeclaration());
    }

    @Test
    public void testGetOpeningHtmlTag() {
        System.out.println("Testing: getOpeningHtmlTag");
        assertEquals(openingHtmlTag, instance.getOpeningHtmlTag());
    }

    @Test
    public void testGetCharsetXmlDeclaration() {
        System.out.println("Testing: getCharsetXmlDeclaration");
        assertEquals(
                charsetXmlDeclaration, instance.getCharsetXmlDeclaration());
    }

    @Test
    public void testGetCharsetMeta() {
        System.out.println("Testing: getCharsetMeta");
        assertNull(instance.getCharsetMeta());
    }

    @Test
    public void testGetCharsetMetaContext() {
        System.out.println("Testing: getCharsetMetaContext");
        assertNull(instance.getCharsetMetaContext());
    }

    @Test
    public void testGetCharsetHttp() {
        System.out.println("Testing: getCharsetHttp");
        assertEquals(charsetHttp, instance.getCharsetHttp());
    }

    @Test
    public void testGetContentType() {
        System.out.println("Testing: getContentType");
        assertEquals(contentType, instance.getContentType());
    }

    @Test
    public void testIsServedAsXml() {
        System.out.println("Testing: isServedAsXml");
        // TODO temp test
        assertFalse(instance.isServedAsXml());
    }
}