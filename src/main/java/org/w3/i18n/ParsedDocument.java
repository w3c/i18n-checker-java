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

import com.ning.http.client.Request;
import com.ning.http.client.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3.assertor.model.Assertion;

/**
 *
 * @author Joseph J Short
 */
public class ParsedDocument {

    private final Request request;
    private final Response response;
    private final String responseBody;
    private final Document document;
    private final String doctypeDeclaration;
    private final DoctypeClassification doctypeClassification;
    private final boolean servedAsXml;
    private final String byteOrderMark;
    private final String xmlDeclaration;
    private final String htmlOpeningTag;
    private final String charsetHttp;
    private final String charsetXmlDeclaration;
    private final String charsetMeta;
    private final String charsetMetaContext;

    public ParsedDocument(Request request, Response response) {
        this.request = request;
        this.response = response;

        // Parse the response body
        String responseBodyT = null;
        try {
            responseBodyT = response.getResponseBody();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        this.responseBody = responseBodyT;
        this.document = Jsoup.parse(responseBody);

        // Find the doctype declaration
        Matcher dtdMatcher = Pattern.compile("<!DOCTYPE[^>]*>")
                .matcher(responseBody.substring(
                0, Math.min(512, responseBody.length())));
        this.doctypeDeclaration = dtdMatcher.find() ? dtdMatcher.group() : null;

        // Classify the doctype
        this.doctypeClassification = classifyDoctype(doctypeDeclaration);
        this.servedAsXml = response.getContentType()
                .matches("application/xhtml+xml");

        this.byteOrderMark = findByteOrderMark(responseBody);

        // Find the XML declaration
        Matcher xmlDeclarationMatcher = Pattern.compile("<\\?xml[^>]+>")
                .matcher(responseBody.substring(
                0, Math.min(512, responseBody.length())));
        this.xmlDeclaration = xmlDeclarationMatcher.find()
                ? xmlDeclarationMatcher.group() : null;

        // Find the HTML opening tag
        // TODO Find a way to get the parser to do this:
        Matcher htmlOpeningTagM = Pattern.compile("<html [^>]*>")
                .matcher(responseBody);
        this.htmlOpeningTag =
                htmlOpeningTagM.find() ? htmlOpeningTagM.group() : null;

        // Find the charset declaration in the http response headers
        List<String> charsetHttpMatches = Utils.getMatchingGroups(
                Pattern.compile("charset=[^;]*"), response.getContentType());
        this.charsetHttp =
                charsetHttpMatches.isEmpty()
                ? null : charsetHttpMatches.get(0).substring(8);

        // Find the charset declaration in the XML declaration
        if (xmlDeclaration != null) {
            List<String> charsetXmlMatches = Utils.getMatchingGroups(
                    Pattern.compile("encoding=\"[^\"]*"), xmlDeclaration);
            charsetXmlDeclaration = charsetXmlMatches.isEmpty()
                    ? null : charsetXmlMatches.get(0).substring(10);
        } else {
            charsetXmlDeclaration = null;
        }

        // Find the charset delartion in the meta tags
        Elements metaElements =
                document.getElementsByTag("meta");
        List<Element> matchingMetaElements = new ArrayList<>();
        for (Element e : metaElements) {
            if (e.outerHtml().matches(".*charset.*")) {
                matchingMetaElements.add(e);
            }
        }
        if (matchingMetaElements.size() == 1) {
            String metaTag = matchingMetaElements.get(0).outerHtml();
            List<String> charsetMatches = Utils.getMatchingGroups(
                    Pattern.compile("charset=\"?[^\";]*"), metaTag);
            if (!charsetMatches.isEmpty()) {
                String group = charsetMatches.get(0).substring(8).trim();
                charsetMeta = group.charAt(0) == '"'
                        ? group.substring(1) : group;
                charsetMetaContext = metaTag;
            } else {
                charsetMeta = null;
                charsetMetaContext = null;
            }
        } else if (matchingMetaElements.isEmpty()) {
            charsetMeta = null;
            charsetMetaContext = null;
        } else {
            /*
             * TODO: There is more than one meta tag with a charset declaration. 
             * Is it correct to decide which one to use and generate a warning?
             */
            charsetMeta = null;
            charsetMetaContext = null;
        }
    }

    public Request getRequest() {
        return request;
    }

    public Response getResponse() {
        return response;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public Document getDocument() {
        return document;
    }

    public String getDoctypeDeclaration() {
        return doctypeDeclaration;
    }

    public String getDoctypeDescription() {
        return doctypeClassification.getDescription();
    }

    public String getByteOrderMark() {
        return byteOrderMark;
    }

    public String getXmlDeclaration() {
        return xmlDeclaration;
    }

    public String getHtmlOpeningTag() {
        return htmlOpeningTag;
    }

    public String getCharsetHttp() {
        return charsetHttp;
    }

    public String getCharsetXmlDeclaration() {
        return charsetXmlDeclaration;
    }

    public String getCharsetMeta() {
        return charsetMeta;
    }

    public String getCharsetMetaContext() {
        return charsetMetaContext;
    }

    private static String findByteOrderMark(String str) {
        if (str == null) {
            throw new NullPointerException();
        }
        String byteOrderMark;
        /*
         * TODO: I may have introduced an error here due to not explicitly
         * handling the character encoding of the string. ~~~~~ Joe S.
         */
        byte[] firstCodePoints = str.substring(0, 3).getBytes();
        if (firstCodePoints[0] == 239
                && firstCodePoints[1] == 187
                && firstCodePoints[2] == 191) {
            byteOrderMark = "UTF-8";
        } else if (firstCodePoints[0] == 254
                && firstCodePoints[1] == 255) {
            byteOrderMark = "UTF-16 (BE)";
        } else if (firstCodePoints[0] == 255
                && firstCodePoints[1] == 254) {
            byteOrderMark = "UTF-16 (LE)";
        } else {
            byteOrderMark = null;
        }
        return byteOrderMark;
    }

    public boolean isServedAsXml() {
        return servedAsXml;
    }

    //public $isHTML = false;
    public boolean isHtml() {
        return doctypeClassification == DoctypeClassification.HTML;
    }

    //public $isHTML5 = false;
    public boolean isHtml5() {
        return doctypeClassification == DoctypeClassification.HTML_5;
    }

    //public $isXHTML10 = false;
    public boolean isXhtml10() {
        return doctypeClassification == DoctypeClassification.XHTML_10
                || doctypeClassification == DoctypeClassification.XHTML_10_RDFA;
    }

    //public $isXHTML11 = false;
    public boolean isXhtml11() {
        return doctypeClassification == DoctypeClassification.XHTML_11
                || doctypeClassification == DoctypeClassification.XHTML_11_RDFA;
    }

    // = isXHTML10 || isXHTML11 
    //public $isXHTML1x = false;
    public boolean isXhtml1X() {
        return doctypeClassification == DoctypeClassification.XHTML_10
                || doctypeClassification == DoctypeClassification.XHTML_10_RDFA
                || doctypeClassification == DoctypeClassification.XHTML_11
                || doctypeClassification == DoctypeClassification.XHTML_11_RDFA;
    }

    //public $isRDFa = false;
    public boolean isRdfA() {
        return doctypeClassification == DoctypeClassification.XHTML_10_RDFA
                || doctypeClassification == DoctypeClassification.XHTML_11_RDFA;
    }

    private static DoctypeClassification classifyDoctype(
            String doctypeDeclaration) {
        DoctypeClassification doctypeClassification;
        if (doctypeDeclaration == null
                || doctypeDeclaration.isEmpty()) {
            // From the old project
            doctypeClassification = DoctypeClassification.HTML;
        } else if (doctypeDeclaration.matches("<!DOCTYPE [^>]*DTD HTML")) {
            doctypeClassification = DoctypeClassification.HTML_5;
        } else if (doctypeDeclaration.matches("<!DOCTYPE HTML>")) {
            doctypeClassification = DoctypeClassification.HTML_5;
        } else if (doctypeDeclaration.matches(
                "<!DOCTYPE [^>]*DTD XHTML(\\+[^ ]+)? 1.0[^>]+")) {
            doctypeClassification = doctypeDeclaration.matches("RDFa")
                    ? DoctypeClassification.XHTML_10_RDFA
                    : DoctypeClassification.XHTML_10;
        } else if (doctypeDeclaration.matches(
                "<!DOCTYPE [^>]*DTD XHTML(\\+[^ ]+)? 1.1[^>]+")) {
            doctypeClassification = doctypeDeclaration.matches("RDFa")
                    ? DoctypeClassification.XHTML_11_RDFA
                    : DoctypeClassification.XHTML_11;
        } else {
            // From the old project
            doctypeClassification = DoctypeClassification.HTML_5;
        }
        return doctypeClassification;
    }

    private enum DoctypeClassification {

        HTML("HTML 4.01"),
        HTML_5("HTML5"),
        XHTML_10("XHTML 1.0"),
        XHTML_10_RDFA("XHTML+RDFa 1.0"),
        XHTML_11("XHTML 1.0"),
        XHTML_11_RDFA("XHTML+RDFa 1.1");
        private final String description;

        private DoctypeClassification(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
