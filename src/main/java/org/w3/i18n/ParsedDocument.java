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

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * A {@code ParsedDocument} takes a {@link DocumentResource} and prepares all
 * the information needed for a {@link Check} to make i18n-based
 * {@link Assertions}.
 *
 * @author Joseph J Short
 */
class ParsedDocument {

    private final DocumentResource documentResource;
    // From the html parser:
    private final Document document;
    private final String doctypeDeclaration;
    private final DoctypeClassification doctypeClassification;
    private final ByteOrderMark byteOrderMark;
    private final boolean utf16;
    private final String xmlDeclaration;
    private final String openingHtmlTag;
    private final String charsetXmlDeclaration;
    private final String charsetMeta;
    private final String charsetMetaContext;
    private final boolean multipleMetas;
    private final String contentType;
    private final boolean servedAsXml;
    private final String charsetHttp;
    // TODO: remove this string:
    private final String documentBody;
    private final Set<String> allCharsetDeclarations;
    private final Set<String> nonUtf8CharsetDeclarations;
    private final Set<String> inDocCharsetDeclarations;

    public ParsedDocument(DocumentResource documentResource) {
        if (documentResource == null) {
            throw new NullPointerException(
                    "<DocumentResource documentResource>");
        }
        this.documentResource = documentResource;

        // TODO: Currently a blocking operation.
        byte[] documentBodyBytes;
        try {
            documentBodyBytes = IOUtils.toByteArray(documentResource.getBody());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        this.byteOrderMark = Utils.findByteOrderMark(documentBodyBytes);

        this.documentBody = new String(documentBodyBytes, byteOrderMark == null
                ? Charset.forName("UTF-8")
                : Charset.forName(byteOrderMark.getCharsetName()));


        // Use the HTML parser on the document body.
        this.document = Jsoup.parse(
                documentBody, documentResource.getUrl().toString());

        // Find the doctype declaration; otherwise declare null.
        Matcher dtdMatcher = Pattern.compile("<!DOCTYPE[^>]*>").matcher(
                documentBody.substring(
                0, Math.min(512, documentBody.length())));
        this.doctypeDeclaration = dtdMatcher.find() ? dtdMatcher.group() : null;

        // Classify the doctype declaration. (See "DoctypeClassification" enum.)
        this.doctypeClassification = classifyDoctype(doctypeDeclaration);


        // "71: $this->isUTF16 = ($bom == 'UTF-16LE' || $bom == 'UTF-16BE')"
        if (byteOrderMark == null) {
            this.utf16 = false;
        } else {
            this.utf16 = byteOrderMark.getCharsetName().toLowerCase()
                    .equals("utf-16");
        }

        // Find the XML declaration; otherwise declare null.
        Matcher xmlDeclarationMatcher = Pattern.compile("<\\?xml [^>]*>")
                .matcher(documentBody.substring(
                0, Math.min(512, documentBody.length())));
        this.xmlDeclaration = xmlDeclarationMatcher.find()
                ? xmlDeclarationMatcher.group() : null;

        // Find the opening HTML tag; otherwise declare null.
        // (TODO: Find a way to get the parser to do this.)
        Matcher openingHtmlTagMatcher = Pattern.compile("<html [^>]*>")
                .matcher(documentBody);
        this.openingHtmlTag = openingHtmlTagMatcher.find()
                ? openingHtmlTagMatcher.group() : null;

        /* Find the character set declaration in the XML declaration; otherwise
         * declare null. */
        if (xmlDeclaration != null) {
            List<String> charsetXmlMatches = Utils.getMatchingGroups(
                    Pattern.compile("encoding=\"[^\"]*"), xmlDeclaration);
            charsetXmlDeclaration = charsetXmlMatches.isEmpty()
                    ? null : charsetXmlMatches.get(0).substring(10);
        } else {
            charsetXmlDeclaration = null;
        }

        /* Find the character set declaration in the "meta" tags; otherwise
         * declare null. */
        Elements metaTagElements =
                document.getElementsByTag("meta");
        List<Element> matchingMetaElements = new ArrayList<>();
        for (Element e : metaTagElements) {
            if (e.outerHtml().matches(".*charset.*")) {
                matchingMetaElements.add(e);
            }
        }
        if (matchingMetaElements.size() > 0) {
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
            multipleMetas = matchingMetaElements.size() > 1;
        } else {
            charsetMeta = null;
            charsetMetaContext = null;
            multipleMetas = false;
        }

        // Find the Content-Type http header and the details within.
        this.contentType = documentResource.getHeader("Content-Type");
        if (contentType != null) {
            List<String> charsetHttpMatches = Utils.getMatchingGroups(
                    Pattern.compile("charset=[^;]*"), contentType);
            this.charsetHttp = charsetHttpMatches.isEmpty()
                    ? null : charsetHttpMatches.get(0).substring(8);
            this.servedAsXml = contentType.matches("application/xhtml+xml");
        } else {
            this.charsetHttp = null;
            this.servedAsXml = false;
        }

        // Aggregate charset declarations.
        this.allCharsetDeclarations = new TreeSet<>();
        this.inDocCharsetDeclarations = new TreeSet<>();
        if (this.charsetHttp != null) {
            String d = this.charsetHttp.trim().toLowerCase();
            this.allCharsetDeclarations.add(d);
        }
        if (this.byteOrderMark != null) {
            String d = this.byteOrderMark.getCharsetName().toLowerCase();
            this.allCharsetDeclarations.add(d);
            this.inDocCharsetDeclarations.add(d);
        }
        if (this.charsetXmlDeclaration != null) {
            String d = this.charsetXmlDeclaration.trim().toLowerCase();
            this.allCharsetDeclarations.add(d);
            this.inDocCharsetDeclarations.add(d);
        }
        if (this.charsetMeta != null) {
            String d = this.charsetMeta.trim().toLowerCase();
            this.allCharsetDeclarations.add(
                    this.charsetMeta.trim().toLowerCase());
            this.inDocCharsetDeclarations.add(d);
        }

        this.nonUtf8CharsetDeclarations = new TreeSet<>();
        for (String charsetDeclaration : this.allCharsetDeclarations) {
            if (!charsetDeclaration.equals("utf-8")) {
                nonUtf8CharsetDeclarations.add(charsetDeclaration);
            }
        }
    }

    public Document getDocument() {
        return document;
    }

    /**
     * Returns the document type declaration (DOCTYPE) of the document, or null
     * if there was none. E.g.
     * "{@code &lt;!DOCTYPE html PUBLIC "-//W3C//DTD XHTML";} ... &gt;".
     *
     * @return the document type declaration (DOCTYPE) of the document; or null
     * if there was none.
     */
    public String getDoctypeDeclaration() {
        return doctypeDeclaration;
    }

    public String getDoctypeDescription() {
        return doctypeClassification.getDescription();
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

    /**
     * Returns the byte order mark (BOM) of the document; or null if there was
     * none.
     *
     * @return the byte order mark (BOM) of the document; or null if there was
     * none.
     */
    public ByteOrderMark getByteOrderMark() {
        return byteOrderMark;
    }

    public boolean isUtf16() {
        return utf16;
    }

    /**
     * Returns the XML declaration of the document; or null if there was none.
     * E.g. "&lt;?xml version="1.0" ... ?&gt;".
     *
     * @return the XML declaration of the document; or null if there was none.
     */
    public String getXmlDeclaration() {
        return xmlDeclaration;
    }

    /**
     * Returns the HTML opening tag of the document; or null if there was none.
     * E.g. "&lt;html xmlns= ... &gt;".
     *
     * @return the HTML opening tag of the document; or null if there was none.
     */
    public String getOpeningHtmlTag() {
        return openingHtmlTag;
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

    public boolean hasMultipleMetas() {
        return multipleMetas;
    }

    public String getCharsetHttp() {
        return charsetHttp;
    }

    public String getContentType() {
        return contentType;
    }

    public DocumentResource getDocumentResource() {
        return documentResource;
    }

    public String getDocumentBody() {
        return documentBody;
    }

    public boolean isServedAsXml() {
        return servedAsXml;
    }

    public Set<String> getAllCharsetDeclarations() {
        return allCharsetDeclarations;
    }

    public Set<String> getNonUtf8CharsetDeclarations() {
        return nonUtf8CharsetDeclarations;
    }

    public Set<String> getInDocCharsetDeclarations() {
        return inDocCharsetDeclarations;
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
                "<!DOCTYPE [^>]*DTD XHTML(\\+[^ ]+)? 1\\.0[^>]+>")) {
            doctypeClassification = doctypeDeclaration.matches("RDFa")
                    ? DoctypeClassification.XHTML_10_RDFA
                    : DoctypeClassification.XHTML_10;
        } else if (doctypeDeclaration.matches(
                "<!DOCTYPE [^>]*DTD XHTML(\\+[^ ]+)? 1\\.1[^>]+")) {
            doctypeClassification = doctypeDeclaration.matches("RDFa")
                    ? DoctypeClassification.XHTML_11_RDFA
                    : DoctypeClassification.XHTML_11;
        } else {
            // From the old project
            doctypeClassification = DoctypeClassification.HTML_5;
        }
        return doctypeClassification;
    }

    /*
     * This enum is for convenience, it saves having to declare multiple
     * booleans (such as "isXhtml10"
     */
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
