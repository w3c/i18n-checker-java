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

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Element;

/**
 * A {@code Check} object represents a stateful process of performing i18n
 * checks on a {@code DocumentResource}.
 *
 * @author Joseph J Short
 */
class Check {

    private final DocumentResource documentResource;
    private final ParsedDocument parsedDocument;
    private final List<Assertion> assertions;

    public Check(DocumentResource documentResource) {
        // Use the DocumentResource to prepare a ParsedDocument.
        this.documentResource = documentResource;
        this.parsedDocument = new ParsedDocument(documentResource);

        // Perform checks.
        this.assertions = new ArrayList<>();

        addAssertionDtd();
        addAssertionCharsetBom();
        addAssertionCharsetXmlDeclaration();
        addAssertionCharsetMeta();
        addAssertionLangAttr();
        addAssertionLangMeta();
        addAssertionDirHtml();
        addAssertionClassID();
        addAssertionMimetype();
        addAssertionCharsetHttp();
        addAssertionLangHttp();
        addAssertionRequestHeaders();
        addAssertionCharsetReports();

        Collections.sort(assertions);
    }

    public DocumentResource getDocumentResource() {
        return documentResource;
    }

    public ParsedDocument getParsedDocument() {
        return parsedDocument;
    }

    public List<Assertion> getAssertions() {
        return assertions;
    }

    private void addAssertionDtd() {
        assertions.add(new Assertion(
                "dtd",
                Assertion.Level.INFO,
                "",
                parsedDocument.getDoctypeDescription(),
                Arrays.asList(parsedDocument.getDoctypeDeclaration())));
    }

    private void addAssertionCharsetBom() {
        assertions.add(new Assertion(
                "charset_bom",
                Assertion.Level.INFO,
                "Byte order mark (BOM)",
                "",
                Arrays.asList(parsedDocument.getByteOrderMark())));
    }

    private void addAssertionCharsetXmlDeclaration() {
        assertions.add(new Assertion(
                "charset_xml", Assertion.Level.INFO, "", "",
                Arrays.asList(parsedDocument.getCharsetXmlDeclaration(),
                parsedDocument.getXmlDeclaration())));
    }

    private void addAssertionCharsetMeta() {
        assertions.add(new Assertion(
                "charset_meta",
                Assertion.Level.INFO,
                "",
                "",
                Arrays.asList(parsedDocument.getCharsetMeta(),
                parsedDocument.getCharsetMetaContext())));
    }

    private void addAssertionLangAttr() {
        // TODO ignores either xml:lang or lang, whichever is first
        String htmlTag = parsedDocument.getOpeningHtmlTag();
        if (htmlTag != null) {
            Matcher langM = Pattern.compile("lang=\"[^\"]*\"").matcher(htmlTag);
            String langAttr = langM.find()
                    ? langM.group().substring(6, langM.group().length() - 1)
                    : null;
            assertions.add(new Assertion(
                    "lang_attr_lang", Assertion.Level.INFO, "", "",
                    Arrays.asList(langAttr, htmlTag)));
        }
    }

    private void addAssertionLangMeta() {
        if (!parsedDocument.getDocument()
                .getElementsByAttributeValue("http-equiv", "Content-Language")
                .isEmpty()) {
            String metaTag = parsedDocument.getDocument()
                    .getElementsByAttributeValue(
                    "http-equiv", "Content-Language").first().outerHtml();
            Matcher contentM =
                    Pattern.compile("content=\"[^\"]*\"").matcher(metaTag);
            String content = contentM.find()
                    ? contentM.group()
                    .substring(9, contentM.group().length() - 1) : null;
            assertions.add(new Assertion(
                    "lang_meta", Assertion.Level.INFO, "", "",
                    Arrays.asList(content, metaTag)));
        } else {
            assertions.add(new Assertion("lang_meta", Assertion.Level.INFO,
                    "", "", new ArrayList<String>()));
        }
    }

    private void addAssertionDirHtml() {
        String htmlOpeningTag = parsedDocument.getOpeningHtmlTag();
        if (htmlOpeningTag != null) {
            Matcher dirAttrM =
                    Pattern.compile("dir\"[^\"]*\"").matcher(htmlOpeningTag);
            String dirAttr = dirAttrM.find() ? dirAttrM.group() : null;
            assertions.add(new Assertion(
                    "dir_default", Assertion.Level.INFO, "", "",
                    dirAttr == null ? new ArrayList<String>()
                    : Arrays.asList(dirAttr, htmlOpeningTag)));
        }
    }

    private void addAssertionClassID() {
        // Aggregate all class names and IDs.
        Set<String> idsAndClassNames = new TreeSet<>();
        for (Element element : this.parsedDocument.getDocument()
                .getElementsByAttribute("class")) {
            idsAndClassNames.addAll(element.classNames());
        }
        for (Element element : this.parsedDocument.getDocument()
                .getElementsByAttribute("id")) {
            idsAndClassNames.add(element.id());
        }

        // Find problematic class names and IDs
        List<String> problems = new ArrayList<>();
        CharsetEncoder ce = Charset.forName("US-ASCII").newEncoder();
        for (String str : idsAndClassNames) {
            if ( // Non-ASCII ...
                    !ce.canEncode(str)
                    // or Non-NFC (Unicode normalisation)
                    || !Normalizer.isNormalized(str, Normalizer.Form.NFC)) {
                problems.add(str);
            }
        }

        // Finally ...
        if (!problems.isEmpty()) {
            assertions.add(
                    new Assertion("class_id", Assertion.Level.INFO,
                    "", "", problems));
        }
    }

    private void addAssertionMimetype() {
        if (parsedDocument.isServedAsXml()) {
            assertions.add(new Assertion(
                    "message_xhtml5_partial_support",
                    Assertion.Level.MESSAGE,
                    "",
                    // TODO: Update description from previous project.
                    "This is an xhtml5 document. xhtml5 specifics are not yet"
                    + " integrated in the checker so you may have inacurate"
                    + " results.",
                    new ArrayList<String>()));
        }
        assertions.add(new Assertion(
                "mimetype",
                Assertion.Level.INFO,
                "",
                parsedDocument.getContentType(),
                new ArrayList<String>()));
    }

    private void addAssertionCharsetHttp() {
        String context = "Content-Type: "
                + parsedDocument.getContentType();
        assertions.add(new Assertion(
                "charset_http", Assertion.Level.INFO, "", "",
                Arrays.asList(parsedDocument.getCharsetHttp(), context)));

    }

    private void addAssertionRequestHeaders() {
        // Find relevant headers.
        /*
         * TODO: Currently there are never any request headers because
         * async-http-client doesn't use any by default.
         */
        Map<String, List<String>> headers =
                documentResource.getHeaders();
        String[] desiredHeaders = {
            "Accept-Language",
            "Accept-Charset"
        };
        List<String> result = new ArrayList<>();
        for (String header : desiredHeaders) {
            if (headers.containsKey(header)) {
                StringBuilder sb = new StringBuilder();
                sb.append(header).append(": ");
                for (String contents : headers.get(header)) {
                    sb.append(contents);
                }
                result.add(sb.toString());
            }
        }
        assertions.add(new Assertion(
                "request_headers", Assertion.Level.INFO, "", "", result));
    }

    private void addAssertionLangHttp() {
        String contentLanguage =
                documentResource.getHeader("Content-Language");
        if (contentLanguage != null) {
            assertions.add(new Assertion(
                    "lang_http",
                    Assertion.Level.INFO,
                    "",
                    "",
                    Arrays.asList(contentLanguage,
                    "Content-Language: " + contentLanguage)));
        }
    }

    private void addAssertionCharsetReports() {
        // Get all the charset declarations.
        List<String> charsetDeclarations = new ArrayList<>();
        if (parsedDocument.getCharsetHttp() != null) {
            charsetDeclarations.add(
                    parsedDocument.getCharsetHttp().toLowerCase());
        }
        if (parsedDocument.getByteOrderMark() != null) {
            charsetDeclarations.add(parsedDocument.getByteOrderMark()
                    /* This removes " (BE)" or " (LE)" from the UTF-16 and
                     * UTF-32 byte order marks. */
                    .toLowerCase().split(" ")[0]);
        }
        if (parsedDocument.getCharsetXmlDeclaration() != null) {
            charsetDeclarations.add(
                    parsedDocument.getCharsetXmlDeclaration().toLowerCase());
        }
        if (parsedDocument.getCharsetMeta() != null) {
            charsetDeclarations.add(
                    parsedDocument.getCharsetMeta().toLowerCase());
        }

        // Report: No charset declarations.
        if (charsetDeclarations.isEmpty()) {
            if (!parsedDocument.isServedAsXml()) {
                Assertion.Level level = parsedDocument.isHtml5()
                        ? Assertion.Level.ERROR : Assertion.Level.WARNING;
                assertions.add(new Assertion("rep_charset_none",
                        level, "", "", new ArrayList<String>()));
            }
        } else {
            // Report: Non UTF-8 declarations.
            List<String> nonUtf8declarations = new ArrayList<>();
            for (String declaration : charsetDeclarations) {
                if (!declaration.matches("utf-8")) {
                    nonUtf8declarations.add(declaration);
                }
            }
            if (!nonUtf8declarations.isEmpty()) {
                assertions.add(new Assertion(
                        "rep_charset_no_utf8", Assertion.Level.ERROR,
                        "", "", nonUtf8declarations));
            }

            // Report: More than one distinct declaration.
            Set distinctDeclarations = new HashSet(charsetDeclarations);
            if (distinctDeclarations.size() > 1) {
                assertions.add(new Assertion(
                        "rep_charset_conflict", Assertion.Level.ERROR,
                        "", "", new ArrayList(distinctDeclarations)));
            }

            // Report: XML tag charset declaration used.
            // TODO these assertions have small differences based on the doctype
            if (parsedDocument.getCharsetXmlDeclaration() != null) {
                if (parsedDocument.isHtml()) {
                    assertions.add(new Assertion(
                            "rep_charset_xml_decl", Assertion.Level.ERROR,
                            "", "", Arrays.asList(
                            parsedDocument.getCharsetXmlDeclaration())));
                } else if (!parsedDocument.isServedAsXml()) {
                    if (parsedDocument.isHtml5()) {
                        assertions.add(new Assertion(
                                "rep_charset_xml_decl", Assertion.Level.ERROR,
                                "", "", Arrays.asList(
                                parsedDocument.getCharsetXmlDeclaration())));
                    } else if (parsedDocument.isXhtml10()) {
                        assertions.add(new Assertion(
                                "rep_charset_xml_decl", Assertion.Level.ERROR,
                                "", "", Arrays.asList(
                                parsedDocument.getCharsetXmlDeclaration())));
                    }
                }
            }

            // Report: Meta charset tag will cause validation to fail.
            if (parsedDocument.getCharsetMeta() != null
                    && !parsedDocument.getCharsetMeta().isEmpty()
                    && !parsedDocument.isHtml5()) {
            }
        }
    }
}
