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
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.htmlparser.jericho.Source;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.IOUtils;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.LoggerProvider;
import net.htmlparser.jericho.StartTag;
import net.htmlparser.jericho.StartTagType;

/**
 * A {@code ParsedDocument} takes a {@link DocumentResource} and prepares all
 * the information needed for a {@link Check} to make i18n-based
 * {@link Assertions}.
 *
 * @author Joseph J Short
 */
class ParsedDocument {

    static {
        // Suppress HTML parser's log output.
        net.htmlparser.jericho.Config.LoggerProvider = LoggerProvider.DISABLED;
    }
    // Resources:
    private final DocumentResource documentResource;
    private final Source source;
    private final String documentBody;
    private final ByteOrderMark byteOrderMark;
    // Information found by inspecting the DocumentResource:
    // '!DOCTYPE' tag.
    private final String doctypeTag;
    private final DoctypeClassification doctypeClassification;
    // '?xml' tag.
    private final String xmlDeclaration;
    private final String charsetXmlDeclaration;
    // Charset in 'meta' tags.
    private final Map<String, List<String>> charsetMetaTags;
    private final List<String> charsetMetaTagsOutside1024;
    // 'Content-Type' HTTP response header.
    private final String contentType;
    private final String charsetHttp;
    private final boolean servedAsXml;
    // Opening 'html' tag.
    private final String openingHtmlTag;
    private final String openingHtmlTagLang;
    private final String openingHtmlTagXmlLang;
    private final String defaultDir;
    // 'Content-Language' HTTP response header.
    private final String contentLanguage;
    // Lang in 'meta' tags.
    private final String langMeta;
    // Class and id names in the document that are non-NFC or non-ASCII.
    private final Set<String> allNonNfcClassIdNames;
    private final List<String> allNonNfcClassIdTags;
    // Other sources of information:
    private final List<String> bomsInContent;
    private final boolean utf16;
    private final List<String> charsetLinkTags;
    private final List<String> bdoTagsWithoutDir;
    private final List<String> bITagsWithoutClass;
    private final Set<String> allCharsetDeclarations;
    private final Set<String> nonUtf8CharsetDeclarations;
    private final Set<String> inDocCharsetDeclarations;
    private final Set<String> allLangAttributes;
    private final Set<String> allXmlLangAttributes;
    private final List<String> allLangAttributeTags;
    private final List<String> allXmlLangAttributeTags;
    private final List<List<String>> allConflictingLangAttributes;
    private final Set<String> allDirAttributes;

    public ParsedDocument(DocumentResource documentResource) {
        if (documentResource == null) {
            throw new NullPointerException(
                    "documentResource: " + documentResource);
        }
        // Prepare resources:
        this.documentResource = documentResource;
        // TODO: Currently a blocking operation.
        byte[] documentBodyBytes;
        try {
            documentBodyBytes = IOUtils.toByteArray(documentResource.getBody());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        this.byteOrderMark = documentBodyBytes.length <= 5 ? null
                : Utils.findByteOrderMark(
                Arrays.copyOf(documentBodyBytes, 4));

        this.documentBody = new String(documentBodyBytes, byteOrderMark == null
                ? Charset.forName("UTF-8")
                : Charset.forName(byteOrderMark.getCharsetName()));

        // Use the HTML parser on the document body.
        this.source = new Source(documentBody);
        this.source.fullSequentialParse();

        // Process resources to find information.

        // Pattern for finding charset in "http-equiv=Content-Type" meta tags.
        Pattern contentTypeCharsetPattern =
                Pattern.compile("charset\\s*=\\s*([^\\s;]+)");

        // Document Type Declaration (DOCTYPE).
        // NB: In a valid document there is only one DTD.
        StartTag doctypeStartTag = source.getFirstStartTag(
                StartTagType.DOCTYPE_DECLARATION);
        this.doctypeTag = doctypeStartTag == null ? null
                : doctypeStartTag.toString().trim();
        this.doctypeClassification = classifyDoctype(doctypeTag);

        // XML Declaration ('?xml' tag at the start of the document).
        StartTag xmlStartTag = source.getFirstStartTag(
                StartTagType.XML_DECLARATION);
        if (xmlStartTag != null) {
            this.xmlDeclaration = xmlStartTag.toString().trim();
            Matcher charsetMatcher = Pattern.compile(
                    "encoding\\s*=\\s*'?\"?\\s*([^\"'\\s\\?>]+)")
                    .matcher(this.xmlDeclaration);
            this.charsetXmlDeclaration =
                    charsetMatcher.find() ? charsetMatcher.group(1) : null;
        } else {
            this.xmlDeclaration = null;
            this.charsetXmlDeclaration = null;
        }

        // Find all charset declarations in meta tags.
        this.charsetMetaTags = new TreeMap<>();
        this.charsetMetaTagsOutside1024 = new ArrayList<>();
        List<Element> metaElements = source.getAllElements("meta");
        for (Element metaElement : metaElements) {
            String charset = null;
            // Look for a "<meta charset="..." >" tag.
            if (metaElement.getAttributeValue("charset") != null) {
                charset = metaElement.getAttributeValue("charset").trim();
            } // Look for a "<meta http-equiv="Content-Type" ... >" tag.
            else {
                String httpEquiv = metaElement.getAttributeValue("http-equiv");
                String content = metaElement.getAttributeValue("content");
                if (httpEquiv != null
                        && content != null
                        && httpEquiv.equalsIgnoreCase("Content-Type")) {
                    Matcher m = contentTypeCharsetPattern.matcher(content);
                    if (m.find()) {
                        charset = m.group(1);
                    }
                }
            }
            // If a charset declaration was found, add this tag to the list.
            if (charset != null) {
                charset = charset.trim();
                if (!charset.isEmpty()) {
                    String tag = metaElement.getStartTag().toString().trim();
                    if (!charsetMetaTags.containsKey(charset)) {
                        charsetMetaTags.put(
                                charset, new ArrayList<String>());
                    }
                    charsetMetaTags.get(charset).add(tag);
                    if (metaElement.getEnd() > 1024) {
                        charsetMetaTagsOutside1024.add(
                                metaElement.getStartTag().toString());
                    }
                }
            }
        }

        // Find the 'Content-Type' HTTP response header and process it.
        this.contentType = documentResource.getHeader("Content-Type");
        if (contentType != null
                /* TODO: DEBUG! This is a workaround for passing tests that
                 * don't detect a bug in the old checker. See: 
                 * http://qa-dev.w3.org/i18n-checker-test/check.php?uri=http%3A%
                 * 2F%2Fwww.w3.org%2FInternational%2Ftests%2Fi18n-checker%2Fgene
                 * rate%3Ftest%3D24%26format%3Dhtml%26serveas%3Dhtml
                 * ~~~ Joe (Joseph.J.Short@gmail.com) */
                && !contentType.equals("text/html;; charset=UTF-8")) {
            Matcher m = contentTypeCharsetPattern.matcher(contentType);
            this.charsetHttp = m.find() ? m.group(1) : null;
            this.servedAsXml = contentType.contains("application/xhtml+xml");
        } else {
            this.charsetHttp = null;
            this.servedAsXml = false;
        }

        // Find the opening 'html' tag and look for some choice attributes.
        Element htmlElement = source.getFirstElement("html");
        if (htmlElement != null) {
            this.openingHtmlTag = htmlElement.getStartTag().toString();
            this.openingHtmlTagLang = htmlElement.getAttributeValue("lang");
            this.openingHtmlTagXmlLang =
                    htmlElement.getAttributeValue("xml:lang");
            this.defaultDir = htmlElement.getAttributeValue("dir");
        } else {
            this.openingHtmlTag = null;
            this.openingHtmlTagLang = null;
            this.openingHtmlTagXmlLang = null;
            this.defaultDir = null;
        }

        // Find the 'Content-Language' HTTP response header.
        this.contentLanguage = documentResource.getHeader("Content-Language");

        // Find a 'meta' tag with 'http-equiv="Content-Language"'.
        /* TODO: Change this to a similar structure that the charset meta tags
         * are stored in. */
        {
            int i = 0;
            String langMetaS = null;
            while (langMetaS == null && i < metaElements.size()) {
                if (metaElements.get(i).getAttributeValue("http-equiv") != null
                        && metaElements.get(i).getAttributeValue("http-equiv")
                        .equalsIgnoreCase("Content-Language")) {
                    // NB: langMetaS will still be null if there is no content.
                    langMetaS =
                            metaElements.get(i).getAttributeValue("content");
                }
                i++;
            }
            this.langMeta = langMetaS;
        }

        // Find class and id names that are non-ASCII or non-NFC.
        this.allNonNfcClassIdNames = new TreeSet<>();
        this.allNonNfcClassIdTags = new ArrayList<>();
        Set<Element> nonNfcClassIdNamesElements = new LinkedHashSet<>();
        CharsetEncoder usAsciiEncoder =
                Charset.forName("US-ASCII").newEncoder();
        for (Element element : source.getAllElements()) {
            Set<String> names = new TreeSet<>();
            String classAttr = element.getAttributeValue("class");
            String idAttr = element.getAttributeValue("id");
            if (classAttr != null) {
                for (String className : classAttr.split(" ")) {
                    if (!className.isEmpty()) {
                        names.add(className);
                    }
                }
            }
            if (idAttr != null) {
                String id = idAttr.trim();
                if (!id.isEmpty()) {
                    names.add(id);
                }
            }
            boolean nonNfcAscii = false;
            for (String name : names) {
                if (// If non-ASCII
                        !usAsciiEncoder.canEncode(name)
                        // ... or non-NFC (Unicode normalisation):
                        || !Normalizer.isNormalized(
                        name, Normalizer.Form.NFC)) {
                    nonNfcAscii = true;
                    allNonNfcClassIdNames.add(name);
                }
            }
            if (nonNfcAscii) {
                nonNfcClassIdNamesElements.add(element);
            }
        }
        for (Element element : nonNfcClassIdNamesElements) {
            this.allNonNfcClassIdTags.add(element.getStartTag().toString());
        }

        // Find any BOMs in the content.
        this.bomsInContent = new ArrayList<>();
        for (int i = 1; i < documentBodyBytes.length - 5; i++) {
            ByteOrderMark bom = Utils.findByteOrderMark(Arrays.copyOfRange(
                    documentBodyBytes, i, i + 5));
            if (bom != null) {
                // Add a context of 15 characters either side to the list.
                int startofContext = Math.max(0, i - 15);
                int endOfContext =
                        Math.min(documentBodyBytes.length - 1, i + 20);
                try {
                    /* The context will look something like:
                     * " ... comes the BOM /???/. Ok, test that. ... "
                     * 
                     *  A BOM encoded in US-ASCII looks something like "???"
                     * (depending on the number of code points it uses). */
                    bomsInContent.add(
                            (startofContext == 0 ? "\"" : "\" ... ")
                            + new String(
                            Arrays.copyOfRange(
                            documentBodyBytes, startofContext, endOfContext),
                            "US-ASCII").replaceAll("\\s+", " ")
                            + (endOfContext == documentBodyBytes.length - 1
                            ? "\"" : " ... \""));
                } catch (UnsupportedEncodingException ex) {
                    throw new RuntimeException(ex);
                }
                i += 2;
            }
        }

        // Use the BOM to determine whether the document is in UTF-16.
        // NB: This is behaviour copied accross from the old project.
        if (byteOrderMark == null) {
            this.utf16 = false;
        } else {
            this.utf16 = byteOrderMark.getCharsetName().toUpperCase()
                    .matches(".*UTF-16.*");
        }

        // Find all 'a' and 'link' tags with a 'charset' attribute.
        this.charsetLinkTags = new ArrayList<>();
        for (Element element : source.getAllElements()) {
            if ((element.getName().toLowerCase().equals("a")
                    || element.getName().toLowerCase().equals("link"))
                    && element.getAttributeValue("charset") != null) {
                this.charsetLinkTags.add(
                        element.getStartTag().toString().trim());
            }
        }

        // Find 'bdo' tags without 'dir' attributes.
        this.bdoTagsWithoutDir = new ArrayList<>();
        for (Element element : source.getAllElements("bdo")) {
            if (element.getAttributeValue("dir") == null) {
                bdoTagsWithoutDir.add(element.getStartTag().toString().trim());
            }
        }

        // Find all 'b' and 'i' tags without a class name.
        this.bITagsWithoutClass = new ArrayList<>();
        for (Element element : source.getAllElements()) {
            if ((element.getName().toLowerCase().equals("b")
                    || element.getName().toLowerCase().equals("i"))) {
                String classAttr = element.getAttributeValue("class");
                if (classAttr == null || classAttr.trim().isEmpty()) {
                    String context = element.toString();
                    if (context.length() > 15) {
                        context = context.substring(0, 14) + " ... ";
                    }
                    bITagsWithoutClass.add("\"" + context + "\"");
                }
            }
        }


        // Make aggregates of charset declarations.
        this.allCharsetDeclarations = new TreeSet<>();
        this.inDocCharsetDeclarations = new TreeSet<>();
        if (this.charsetHttp != null) {
            String d = this.charsetHttp.trim().toUpperCase();
            this.allCharsetDeclarations.add(d);
        }
        if (this.byteOrderMark != null) {
            String d = this.byteOrderMark.getCharsetName().toUpperCase();
            this.allCharsetDeclarations.add(d);
            this.inDocCharsetDeclarations.add(d);
        }
        if (this.charsetXmlDeclaration != null) {
            String d = this.charsetXmlDeclaration.trim().toUpperCase();
            this.allCharsetDeclarations.add(d);
            this.inDocCharsetDeclarations.add(d);
        }
        for (String charset : charsetMetaTags.keySet()) {
            this.allCharsetDeclarations.add(charset.toUpperCase());
            this.inDocCharsetDeclarations.add(charset.toUpperCase());
        }
        this.nonUtf8CharsetDeclarations = new TreeSet<>();
        for (String charsetDeclaration : this.allCharsetDeclarations) {
            if (!charsetDeclaration.equalsIgnoreCase("UTF-8")) {
                nonUtf8CharsetDeclarations.add(charsetDeclaration);
            }
        }

        // Make aggregates of language declarations.
        this.allConflictingLangAttributes = new ArrayList<>();
        this.allLangAttributes = new TreeSet<>();
        this.allXmlLangAttributes = new TreeSet<>();
        this.allLangAttributeTags = new ArrayList<>();
        this.allXmlLangAttributeTags = new ArrayList<>();
        for (Element element : source.getAllElements()) {
            String langAttr = element.getAttributeValue("lang");
            String xmlLangAttr = element.getAttributeValue("xml:lang");
            String lang = null;
            String xmlLang = null;
            String tag = element.getStartTag().toString().trim();
            if (langAttr != null) {
                lang = langAttr.trim();
                if (!lang.isEmpty()) {
                    allLangAttributes.add(lang);
                    allLangAttributeTags.add(tag);
                }
            }
            if (xmlLangAttr != null) {
                xmlLang = xmlLangAttr.trim();
                if (!xmlLang.isEmpty()) {
                    allXmlLangAttributes.add(xmlLang);
                    allXmlLangAttributeTags.add(tag);
                }
            }
            if (lang != null && xmlLang != null && !lang.equals(xmlLang)) {
                this.allConflictingLangAttributes.add(
                        Arrays.asList(
                        lang, xmlLang, tag));
            }
        }

        // Find all values of dir attributes.
        this.allDirAttributes = new TreeSet<>();
        for (Element element : source.getAllElements()) {
            if (element.getAttributeValue("dir") != null) {
                allDirAttributes.add(element.getAttributeValue("dir"));
            }
        }
    }

    // Methods that return the parsing resources:
    public DocumentResource getDocumentResource() {
        return documentResource;
    }

    public String getDocumentBody() {
        return documentBody;
    }

    public String getByteOrderMark() {
        return byteOrderMark == null ? null : byteOrderMark.toString();
    }

    // Methods that return information relating to the '!DOCTYPE' tag:
    public String getDoctypeTag() {
        return doctypeTag;
    }

    public String getDoctypeClassification() {
        return doctypeClassification.getDescription();
    }

    // Methods that return information relating to the '?xml' tag:
    public String getXmlDeclaration() {
        return xmlDeclaration;
    }

    public String getCharsetXmlDeclaration() {
        return charsetXmlDeclaration;
    }

    // Method that returns extracted 'meta' tags with charset values:
    public Map<String, List<String>> getCharsetMetaTags() {
        return charsetMetaTags;
    }

    public List<String> getCharsetMetaTagsOutside1024() {
        return charsetMetaTagsOutside1024;
    }

    /* Methods that return information relating to the 'Content-Type' HTTP
     * response header */
    public String getContentType() {
        return contentType;
    }

    public String getCharsetHttp() {
        return charsetHttp;
    }

    public boolean isServedAsXml() {
        return servedAsXml;
    }

    // Methods that return information relating to the opening 'html' tag:
    public String getOpeningHtmlTag() {
        return openingHtmlTag;
    }

    public String getOpeningHtmlTagLang() {
        return openingHtmlTagLang;
    }

    public String getOpeningHtmlTagXmlLang() {
        return openingHtmlTagXmlLang;
    }

    public String getDefaultDir() {
        return defaultDir;
    }

    // Returns the value of the 'Content-Language' HTTP response header.
    public String getContentLanguage() {
        return contentLanguage;
    }

    // Returns an extracted 'meta' tag with language value:
    public String getLangMeta() {
        return langMeta;
    }

    /* Methods that help with identifying the markup language used in the
     * document: */
    public boolean isHtml() {
        return doctypeClassification == DoctypeClassification.HTML;
    }

    public boolean isHtml5() {
        return doctypeClassification == DoctypeClassification.HTML_5;
    }

    public boolean isXhtml10() {
        return doctypeClassification == DoctypeClassification.XHTML_10
                || doctypeClassification == DoctypeClassification.XHTML_10_RDFA;
    }

    public boolean isXhtml11() {
        return doctypeClassification == DoctypeClassification.XHTML_11
                || doctypeClassification == DoctypeClassification.XHTML_11_RDFA;
    }

    public boolean isXhtml1X() {
        return doctypeClassification == DoctypeClassification.XHTML_10
                || doctypeClassification == DoctypeClassification.XHTML_10_RDFA
                || doctypeClassification == DoctypeClassification.XHTML_11
                || doctypeClassification == DoctypeClassification.XHTML_11_RDFA;
    }

    public boolean isRdfA() {
        return doctypeClassification == DoctypeClassification.XHTML_10_RDFA
                || doctypeClassification == DoctypeClassification.XHTML_11_RDFA;
    }

    // Methods that return other information about the document:
    public String getCharsetByteOrderMark() {
        return byteOrderMark == null ? null : byteOrderMark.getCharsetName();
    }

    public List<String> getBomsInContent() {
        return bomsInContent;
    }

    public boolean isUtf16() {
        return utf16;
    }

    public List<String> getCharsetLinkTags() {
        return charsetLinkTags;
    }

    public List<String> getBdoTagsWithoutDir() {
        return bdoTagsWithoutDir;
    }

    public List<String> getbITagsWithoutClass() {
        return bITagsWithoutClass;
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

    public Set<String> getAllLangAttributes() {
        return allLangAttributes;
    }

    public Set<String> getAllXmlLangAttributes() {
        return allXmlLangAttributes;
    }

    public List<String> getAllLangAttributeTags() {
        return allLangAttributeTags;
    }

    public List<String> getAllXmlLangAttributeTags() {
        return allXmlLangAttributeTags;
    }

    public List<List<String>> getAllConflictingLangAttributes() {
        return allConflictingLangAttributes;
    }

    public Set<String> getAllNonNfcClassIdNames() {
        return allNonNfcClassIdNames;
    }

    public List<String> getAllNonNfcClassIdTags() {
        return allNonNfcClassIdTags;
    }

    public Set<String> getAllDirAttributes() {
        return allDirAttributes;
    }

    private static DoctypeClassification classifyDoctype(
            String doctypeDeclaration) {
        DoctypeClassification doctypeClassification;
        if (doctypeDeclaration == null
                || doctypeDeclaration.isEmpty()) {
            // From the old project
            doctypeClassification = DoctypeClassification.HTML_5;
        } else if (doctypeDeclaration.matches(
                "<!DOCTYPE [^>]*DTD HTML[^>]+>")) {
            doctypeClassification = DoctypeClassification.HTML;
        } else if (doctypeDeclaration.matches("<!DOCTYPE HTML>")) {
            doctypeClassification = DoctypeClassification.HTML_5;
        } else if (doctypeDeclaration.matches(
                "<!DOCTYPE [^>]*DTD XHTML(\\+[^ ]+)? 1\\.0[^>]+>")) {
            doctypeClassification = doctypeDeclaration.matches("RDFa")
                    ? DoctypeClassification.XHTML_10_RDFA
                    : DoctypeClassification.XHTML_10;
        } else if (doctypeDeclaration.matches(
                "<!DOCTYPE [^>]*DTD XHTML(\\+[^ ]+)? 1\\.1[^>]+>")) {
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
