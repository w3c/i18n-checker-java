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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * A {@code Check} object represents the process of performing an i18n check on
 * a {@link ParsedDocument}. The results of a check are given as a {@code List}
 * of {@link Assertion}, accessed using the {@link #getAssertions()} method.
 *
 * @author Joseph J Short
 */
class Check {

    private final ParsedDocument parsedDocument;
    private final List<Assertion> assertions;

    /**
     * Creates a {@code Check} object which immediately checks the i18n details
     * of the given {@link ParsedDocument}, and places the results in to a
     * {@code List} of {@link Assertion}. The results can be accessed via the
     * {@link #getAssertions()} method.
     *
     * @param parsedDocument the {@link ParsedDocument} to check.
     */
    public Check(ParsedDocument parsedDocument) {
        if (parsedDocument == null) {
            throw new NullPointerException("parsedDocument: " + parsedDocument);
        }
        this.parsedDocument = parsedDocument;
        this.assertions = new ArrayList<>();

        // Check for a bad ParsedDocument.
        // TODO: I think it might be more helpful to throw an exception. ~~~ Joe
        boolean hasDocumentBody = parsedDocument.getDocumentBody() != null
                ? !parsedDocument.getDocumentBody().isEmpty()
                : false;
        if (hasDocumentBody) {
            check();
        } else {
            assertions.add(new Assertion(
                    "no_content",
                    Assertion.Level.MESSAGE,
                    "No content to check",
                    "Either the document was empty or the contents could not be"
                    + " retrieved.",
                    new ArrayList<String>()));
        }
    }

    /**
     * Calls all the 'addAssertionXYZ()' methods.
     */
    private void check() {
        // Add information assertions.
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

        // Add report assertions.
        // (Charset.)
        addAssertionRepCharset1024Limit();
        addAssertionRepCharsetBogusUtf16();
        addAssertionRepCharsetBomFound();
        addAssertionRepCharsetBomInContent();
        addAssertionRepCharsetCharsetAttr();
        addAssertionRepCharsetConflict();
        addAssertionRepCharsetIncorrectUseMeta();
        addAssertionRepCharsetMetaCharsetInvalid();
        addAssertionRepCharsetMetaIneffective();
        addAssertionRepCharsetMultipleMeta();
        addAssertionRepCharsetNoEffectiveCharset();
        addAssertionRepCharsetNoEncodingXml();
        addAssertionRepCharsetNoInDoc();
        addAssertionRepCharsetNoUtf8();
        addAssertionRepCharsetNoVisibleCharset();
        addAssertionRepCharsetNone();
        addAssertionRepCharsetPragma();
        addAssertionRepCharsetUtf16Meta();
        addAssertionRepCharsetUtf16lebe();
        addAssertionRepCharsetXmlDeclUsed();
        // (Lang.)
        addAssertionRepLangConflict();
        addAssertionRepLangContentLangMeta();
        addAssertionRepLangHtmlNoEffectiveLang();
        addAssertionRepLangMalformedAttr();
        addAssertionRepLangMissingHtmlAttr();
        addAssertionRepLangMissingXmlAttr();
        addAssertionRepLangNoLangAttr();
        addAssertionRepLangXmlAttrInHtml();
        // (NonLatin.)
        addAssertionRepLatinNonNfc();
        // (Markup.)
        addAssertionRepMarkupBdoNoDir();
        addAssertionRepMarkupDirIncorrect();
        addAssertionRepMarkupTagsNoClass();

        Collections.sort(assertions);
    }

    /**
     * Returns the {@link ParsedDocument} used to construct this instance.
     *
     * @return the {@link ParsedDocument} used to construct this instance.
     */
    public ParsedDocument getParsedDocument() {
        return parsedDocument;
    }

    /**
     * Returns the results of the i18n check.
     *
     * @return the results of the i18n check.
     */
    public List<Assertion> getAssertions() {
        return assertions;
    }

    /**
     * Document Type Declaration (DOCTYPE). Context has a DOCTYPE classification
     * (e.g. "XHTML 1.1") and the original '!DOCTYPE' tag.
     */
    private void addAssertionDtd() {
        if (parsedDocument.getDoctypeTag() != null) {
            assertions.add(AssertionProvider.getForWith(
                    "info_dtd",
                    Assertion.Level.INFO,
                    Arrays.asList(
                    parsedDocument.getDoctypeClassification(),
                    parsedDocument.getDoctypeTag())));
        }
    }

    /**
     * Charset from Byte Order Mark (BOM). Context has the name of the charset
     * and the name of the BOM.
     */
    private void addAssertionCharsetBom() {
        if (parsedDocument.getByteOrderMark() != null) {
            assertions.add(AssertionProvider.getForWith(
                    "info_charset_bom",
                    Assertion.Level.INFO,
                    Arrays.asList(
                    parsedDocument.getCharsetByteOrderMark(),
                    parsedDocument.getByteOrderMark())));
        }
    }

    /**
     * Charset from XML declaration ('?xml' tag at the start of the document).
     * Context has the name of the character encoding and the original tag.
     */
    private void addAssertionCharsetXmlDeclaration() {
        if (parsedDocument.getCharsetXmlDeclaration() != null) {
            assertions.add(AssertionProvider.getForWith(
                    "info_charset_xml",
                    Assertion.Level.INFO,
                    Arrays.asList(
                    parsedDocument.getCharsetXmlDeclaration(),
                    parsedDocument.getXmlDeclaration())));
        }
    }

    /**
     * Charset(s) from 'meta' tags (e.g. "<meta charset="utf-8">"). Context has
     * a list of charset names followed by a list of corresponding 'meta' tags.
     * Ideally there should be just one such tag in the document.
     */
    private void addAssertionCharsetMeta() {
        if (!parsedDocument.getCharsetMetaTags().isEmpty()) {
            ArrayList<String> contexts = new ArrayList<>();
            contexts.addAll(parsedDocument.getCharsetMetaTags().keySet());
            for (List<String> tags
                    : parsedDocument.getCharsetMetaTags().values()) {
                contexts.addAll(tags);
            }
            assertions.add(AssertionProvider.getForWith(
                    "info_charset_meta",
                    Assertion.Level.INFO,
                    contexts));
        }
    }

    /**
     * Charset from 'Content-Type' HTTP response header. Context has the charset
     * name and the header verbatim (e.g. "Content-Type: text/html;
     * charset=UTF-8").
     */
    private void addAssertionCharsetHttp() {
        if (parsedDocument.getCharsetHttp() != null) {
            assertions.add(AssertionProvider.getForWith(
                    "info_charset_http",
                    Assertion.Level.INFO,
                    Arrays.asList(parsedDocument.getCharsetHttp(),
                    "Content-Type: " + parsedDocument.getContentType())));
        }
    }

    /**
     * Language from a 'lang' and/or 'xml:lang' attribute in opening 'html' tag.
     * Context has the value of the attributes and the original 'html' tag.
     */
    private void addAssertionLangAttr() {
        // Only distinct values of the two attributes are added to the context.
        Set<String> langs = new TreeSet<>();
        if (parsedDocument.getOpeningHtmlTagLang() != null) {
            langs.add(parsedDocument.getOpeningHtmlTagLang());
        }
        if (parsedDocument.getOpeningHtmlTagXmlLang() != null) {
            langs.add(parsedDocument.getOpeningHtmlTagXmlLang());
        }
        if (!langs.isEmpty()) {
            List<String> contexts = new ArrayList<>(langs);
            contexts.add(parsedDocument.getOpeningHtmlTag());
            assertions.add(AssertionProvider.getForWith(
                    "info_lang_attr_lang",
                    Assertion.Level.INFO,
                    contexts));
        }
    }

    /**
     * Language from the 'Content-Language' HTTP response header. Context has
     * the value of the header (should be a language code) and the header
     * verbatim (e.g. "Content-Language: en").
     */
    private void addAssertionLangHttp() {
        if (parsedDocument.getContentLanguage() != null) {
            assertions.add(AssertionProvider.getForWith(
                    "info_lang_http",
                    Assertion.Level.INFO,
                    Arrays.asList(
                    parsedDocument.getContentLanguage(),
                    "Content-Language: "
                    + parsedDocument.getContentLanguage())));
        }
    }

    /**
     * Language from a 'meta' tag with a 'http-equiv' attribute set to
     * 'Content-Language'. Context has the value of the 'content' attribute
     * (should be a language code) and the original tag (e.g. "<meta
     * http-equiv="Content-Language" value="de">").
     */
    private void addAssertionLangMeta() {
        if (parsedDocument.getLangMeta() != null) {
            assertions.add(AssertionProvider.getForWith(
                    "info_lang_meta",
                    Assertion.Level.INFO,
                    Arrays.asList(parsedDocument.getLangMeta())));
        }
    }

    /**
     * Default text-direction given by 'dir' attribute in the opening 'html'
     * tag. Context has the value of the attribute (should be 'ltr', 'rtl', or
     * 'auto') and the opening 'html' tag.
     */
    private void addAssertionDirHtml() {
        if (parsedDocument.getDefaultDir() != null) {
            assertions.add(AssertionProvider.getForWith(
                    "info_dir_default",
                    Assertion.Level.INFO,
                    Arrays.asList(
                    parsedDocument.getDefaultDir(),
                    parsedDocument.getOpeningHtmlTag())));
        }
    }

    /**
     * Class or id names used in the document that are non-NFC (a Unicode
     * normalisation form) or non-ASCII. Context has a list of the names
     * followed by a list of the original opening tags.
     */
    private void addAssertionClassID() {
        if (!parsedDocument.getAllNonNfcClassIdNames().isEmpty()) {
            assertions.add(AssertionProvider.getForWith(
                    "info_class_id",
                    Assertion.Level.INFO,
                    new ArrayList<>(
                    parsedDocument.getAllNonNfcClassIdNames())));
        }
    }

    /**
     * Mimetype given by of the 'Content-Type' HTTP response header. Context has
     * the value of the header.
     */
    private void addAssertionMimetype() {
        if (parsedDocument.getContentType() != null) {
            assertions.add(AssertionProvider.getForWith(
                    "info_mimetype",
                    Assertion.Level.INFO,
                    Arrays.asList(parsedDocument.getContentType())));
        }
    }

    /**
     * HTTP request headers sent when retrieving a remote document. Context has
     * the 'Accept-Language' and 'Accept-Charset' headers verbatim. (These are
     * the only two headers which might affect the i18n of the response.)
     */
    private void addAssertionRequestHeaders() {
        /* TODO: Currently there are never any request headers because
         * async-http-client doesn't use any by default. */
        // Retrieve the desired headers.
        Map<String, List<String>> headers =
                parsedDocument.getDocumentResource().getHeaders();
        String[] desiredHeaders = {
            "Accept-Language",
            "Accept-Charset"
        };
        List<String> contexts = new ArrayList<>();
        for (String headerName : desiredHeaders) {
            if (headers.containsKey(headerName)) {
                /* Build a string representation of the header by appending the
                 * header contents to the header name and a colon. */
                StringBuilder sb = new StringBuilder();
                sb.append(headerName).append(": ");
                for (String headerContents : headers.get(headerName)) {
                    sb.append(headerContents);
                }
                contexts.add(sb.toString());
            }
        }
        if (!contexts.isEmpty()) {
            assertions.add(AssertionProvider.getForWith(
                    "info_request_headers",
                    Assertion.Level.INFO,
                    contexts));
        }
    }

    /**
     * Charset report: Document has a 'meta' tag with a charset declaration
     * outside of the first 1024 byte in the document. Context has a list of the
     * offending 'meta' tags verbatim.
     */
    private void addAssertionRepCharset1024Limit() {
        if (parsedDocument.isHtml5()
                && !parsedDocument.getCharsetMetaTagsOutside1024().isEmpty()) {
            assertions.add(AssertionProvider.getForWith(
                    "rep_charset_1024_limit",
                    Assertion.Level.ERROR,
                    parsedDocument.getCharsetMetaTagsOutside1024()));
        }
    }

    /**
     * Charset report: UTF-16 encoding declaration in a non-UTF-16 document.
     * Context has a list of offending meta tags verbatim.
     */
    private void addAssertionRepCharsetBogusUtf16() {
        if (!parsedDocument.isUtf16()) {
            List<String> contexts = new ArrayList<>();
            for (Map.Entry<String, List<String>> entry
                    : parsedDocument.getCharsetMetaTags().entrySet()) {
                if (entry.getKey().toUpperCase().matches(".*UTF-16.*")) {
                    contexts.addAll(entry.getValue());
                }
            }
            if (!contexts.isEmpty()) {
                assertions.add(AssertionProvider.getForWith(
                        "rep_charset_bogus_utf16",
                        Assertion.Level.ERROR,
                        contexts));
            }
        }
    }

    /**
     * Charset report: UTF-8 BOM found at start of file. Context has the name of
     * the BOM.
     */
    private void addAssertionRepCharsetBomFound() {
        if (parsedDocument.getByteOrderMark() != null
                && parsedDocument.getCharsetByteOrderMark().trim()
                .toLowerCase().matches(".*utf-8.*")) {
            assertions.add(AssertionProvider.getForWith(
                    "rep_charset_bom_found",
                    Assertion.Level.WARNING,
                    Arrays.asList(parsedDocument.getByteOrderMark())));
        }
    }

    /**
     * Charset report: Byte order mark (BOM) found in the document body. Context
     * has a list problem excerpts from the document. The excerpts are around 30
     * characters long, have the invalid BOM in the middle, and are re-encoded
     * in US-ASCII so that the BOM will look like: "???".
     */
    private void addAssertionRepCharsetBomInContent() {
        if (!parsedDocument.getBomsInContent().isEmpty()) {
            assertions.add(AssertionProvider.getForWith(
                    "rep_charset_bom_in_content",
                    Assertion.Level.WARNING,
                    parsedDocument.getBomsInContent()));
        }
    }

    /**
     * Charset report: 'charset' attribute used on an 'a' tag or a 'link' tag.
     * This is an error in HTML5 (because the HTML5 specification deprecates the
     * use of the attribute on these tags). Context has a list of the problem
     * tags verbatim.
     */
    private void addAssertionRepCharsetCharsetAttr() {
        if (!parsedDocument.getCharsetLinkTags().isEmpty()) {
            assertions.add(AssertionProvider.getForWith(
                    "rep_charset_charset_attr",
                    parsedDocument.isHtml5()
                    ? Assertion.Level.ERROR : Assertion.Level.WARNING,
                    parsedDocument.getCharsetLinkTags()));
        }
    }

    /**
     * Charset report: Conflicting character encoding declarations. Context has
     * a list of all the declared charsets.
     */
    private void addAssertionRepCharsetConflict() {
        if (parsedDocument.getAllCharsetDeclarations().size() > 1) {
            assertions.add(AssertionProvider.getForWith(
                    "rep_charset_conflict",
                    Assertion.Level.ERROR,
                    /* TODO: This context would be more helpful if it also
                     * provided the origins of the declarations. */
                    new ArrayList<>(
                    parsedDocument.getAllCharsetDeclarations())));
        }
    }

    /**
     * Charset report: Incorrect use of meta encoding declaration. Context has a
     * verbatim list of all the 'meta' tags that have a charset declaration.
     */
    private void addAssertionRepCharsetIncorrectUseMeta() {
        if (!parsedDocument.getCharsetMetaTags().isEmpty()
                && parsedDocument.getCharsetHttp() == null
                && parsedDocument.getByteOrderMark() == null
                && parsedDocument.getCharsetXmlDeclaration() == null
                && parsedDocument.isXhtml1X()) {
            boolean acceptableMeta = false;
            for (String charset
                    : parsedDocument.getCharsetMetaTags().keySet()) {
                if (charset.toUpperCase().matches(".*UTF-8.*")
                        || charset.toUpperCase().matches(".*UTF-16.*")) {
                    acceptableMeta = true;
                }
            }
            if (!acceptableMeta) {
                List<String> contexts = new ArrayList<>();
                for (List<String> list : parsedDocument.getCharsetMetaTags()
                        .values()) {
                    contexts.addAll(list);
                }
                assertions.add(AssertionProvider.getForWith(
                        "rep_charset_incorrect_use_meta",
                        parsedDocument.isServedAsXml()
                        ? Assertion.Level.ERROR : Assertion.Level.WARNING,
                        contexts));
            }
        }
    }

    /**
     * Charset report: Meta charset tag will cause validation to fail. Context
     * has a list of the problem 'meta' tags verbatim.
     */
    private void addAssertionRepCharsetMetaCharsetInvalid() {
        List<String> contexts = new ArrayList<>();
        if (!parsedDocument.isHtml5()
                && !parsedDocument.getCharsetMetaTags().isEmpty()) {
            for (Map.Entry<String, List<String>> charsetMetaDeclaration
                    : parsedDocument.getCharsetMetaTags().entrySet()) {
                boolean metaCharsetInvalid = false;
                for (String metaTag : charsetMetaDeclaration.getValue()) {
                    if (!metaTag.contains("http-equiv=")
                            && !metaTag.contains("content=")) {
                        metaCharsetInvalid = true;
                    }
                }
                if (metaCharsetInvalid) {
                    contexts.addAll(charsetMetaDeclaration.getValue());
                }
            }
        }
        if (!contexts.isEmpty()) {
            for (List<String> list : parsedDocument.getCharsetMetaTags()
                    .values()) {
                contexts.addAll(list);
            }
            assertions.add(AssertionProvider.getForWith(
                    "rep_charset_meta_charset_invalid",
                    Assertion.Level.WARNING,
                    contexts));
        }
    }

    /**
     * Charset report: Meta encoding declarations don't work with XML. Context
     * has a verbatim list of all the 'meta' tags that have a charset
     * declaration.
     */
    private void addAssertionRepCharsetMetaIneffective() {
        if (!parsedDocument.getCharsetMetaTags().isEmpty()
                && parsedDocument.isServedAsXml()) {
            List<String> contexts = new ArrayList<>();
            for (List<String> list : parsedDocument.getCharsetMetaTags()
                    .values()) {
                contexts.addAll(list);
            }
            assertions.add(AssertionProvider.getForWith(
                    "rep_charset_meta_ineffective",
                    Assertion.Level.INFO,
                    contexts));
        }
    }

    /**
     * Charset report: Multiple encoding declarations using the meta tag.
     * Context has a verbatim list of all the 'meta' tags that have a charset
     * declaration.
     */
    private void addAssertionRepCharsetMultipleMeta() {
        if (parsedDocument.getCharsetMetaTags().size() > 1) {
            List<String> contexts = new ArrayList<>();
            for (List<String> list : parsedDocument.getCharsetMetaTags()
                    .values()) {
                contexts.addAll(list);
            }
            assertions.add(AssertionProvider.getForWith(
                    "rep_charset_multiple_meta",
                    Assertion.Level.ERROR,
                    contexts));
        }
    }

    /**
     * Charset report: No effective character encoding information. Context is
     * empty.
     */
    private void addAssertionRepCharsetNoEffectiveCharset() {
        if (parsedDocument.getCharsetXmlDeclaration() != null
                && parsedDocument.getCharsetHttp() == null
                && parsedDocument.getByteOrderMark() == null
                && parsedDocument.getCharsetMetaTags().isEmpty()
                && (parsedDocument.isHtml()
                || parsedDocument.isHtml5()
                || (parsedDocument.isXhtml10()
                && !parsedDocument.isServedAsXml()))) {
            assertions.add(AssertionProvider.getForWith(
                    "rep_charset_no_effective_charset",
                    Assertion.Level.WARNING,
                    new ArrayList<String>()));
        }
    }

    /**
     * Charset report: No character encoding information. Context is empty. (NB:
     * Similar to 'addAssertionRepCharsetNone()'.)
     */
    private void addAssertionRepCharsetNoEncodingXml() {
        if (parsedDocument.getAllCharsetDeclarations().isEmpty()
                && parsedDocument.isServedAsXml()) {
            assertions.add(AssertionProvider.getForWith(
                    "rep_charset_no_encoding_xml",
                    Assertion.Level.WARNING,
                    new ArrayList<String>()));
        }
    }

    /**
     * Charset report: No charset declaration in the document. Context is empty.
     */
    private void addAssertionRepCharsetNoInDoc() {
        if (parsedDocument.getInDocCharsetDeclarations().isEmpty()
                && !parsedDocument.getAllCharsetDeclarations().isEmpty()) {
            assertions.add(AssertionProvider.getForWith(
                    "rep_charset_no_in_doc",
                    Assertion.Level.WARNING,
                    new ArrayList<String>()));
        }
    }

    /**
     * Charset report: Non-UTF8 character encoding declared. Context has a list
     * of the declared non-UTF8 charsets.
     */
    private void addAssertionRepCharsetNoUtf8() {
        if (!parsedDocument.getNonUtf8CharsetDeclarations().isEmpty()) {
            assertions.add(AssertionProvider.getForWith(
                    "rep_charset_no_utf8",
                    Assertion.Level.INFO,
                    /* TODO: This context would be more helpful if it also
                     * provided the origins of the declarations. */
                    new ArrayList<>(
                    parsedDocument.getNonUtf8CharsetDeclarations())));
        }
    }

    /**
     * Charset report: No visible in-document encoding specified. Context is
     * empty.
     */
    private void addAssertionRepCharsetNoVisibleCharset() {
        if (parsedDocument.getByteOrderMark() != null
                && parsedDocument.getCharsetXmlDeclaration() == null
                && parsedDocument.getCharsetMetaTags().isEmpty()) {
            if (parsedDocument.getCharsetByteOrderMark().toUpperCase()
                    .matches(".*UTF-8.*")) {
                assertions.add(AssertionProvider.getForWith(
                        "rep_charset_no_visible_charset",
                        Assertion.Level.WARNING,
                        new ArrayList<String>()));
            }
        }
    }

    /**
     * Charset report: No character encoding information. Context is empty. (NB:
     * Similar to 'addAssertionRepCharsetNoEncodingXml()'.)
     */
    private void addAssertionRepCharsetNone() {
        if (parsedDocument.getAllCharsetDeclarations().isEmpty()) {
            if (!parsedDocument.isServedAsXml()) {
                assertions.add(AssertionProvider.getForWith(
                        "rep_charset_none",
                        parsedDocument.isHtml5()
                        ? Assertion.Level.ERROR : Assertion.Level.WARNING,
                        new ArrayList<String>()));
            }
        }
    }

    /**
     * Charset report: Meta charset declaration uses 'http-equiv'. Context has a
     * list of the problem 'meta' tags verbatim.
     */
    private void addAssertionRepCharsetPragma() {
        if (parsedDocument.isHtml5()) {
            List<String> contexts = new ArrayList<>();
            for (List<String> list : parsedDocument
                    .getCharsetMetaTags().values()) {
                for (String context : list) {
                    if (context.toLowerCase().matches(".*http-equiv.*")) {
                        contexts.add(context);
                    }
                }
            }
            if (!contexts.isEmpty()) {
                assertions.add(AssertionProvider.getForWith(
                        "rep_charset_pragma", Assertion.Level.INFO, contexts));
            }
        }
    }

    /**
     * Charset report: Meta character encoding declaration used in UTF-16 page.
     * Context has a list of the problem meta tags verbatim.
     */
    private void addAssertionRepCharsetUtf16Meta() {
        if (parsedDocument.isUtf16() && parsedDocument.isHtml5()) {
            List<String> contexts = new ArrayList<>();
            for (Map.Entry<String, List<String>> entry
                    : parsedDocument.getCharsetMetaTags().entrySet()) {
                if (entry.getKey().toUpperCase().matches(".*UTF-16.*")) {
                    contexts.addAll(entry.getValue());
                }
            }
            if (!contexts.isEmpty()) {
                assertions.add(AssertionProvider.getForWith(
                        "rep_charset_utf16_meta", Assertion.Level.ERROR,
                        contexts));
            }
        }
    }

    /**
     * Charset report: UTF-16LE or UTF-16BE found in a character encoding
     * declaration. Context has a list of the charset declarations in their
     * original contexts.
     */
    private void addAssertionRepCharsetUtf16lebe() {
        Map<String, List<String>> nonBomCharsets = new TreeMap<>();
        List<String> contexts = new ArrayList<>();
        if (parsedDocument.getCharsetHttp() != null) {
            nonBomCharsets.put(parsedDocument.getCharsetHttp(),
                    Arrays.asList("Content-Type: "
                    + parsedDocument.getContentType()));
        }
        if (parsedDocument.getCharsetXmlDeclaration() != null) {
            nonBomCharsets.put(parsedDocument.getCharsetXmlDeclaration(),
                    Arrays.asList(parsedDocument.getXmlDeclaration()));
        }
        nonBomCharsets.putAll(parsedDocument.getCharsetMetaTags());
        for (Map.Entry<String, List<String>> entry
                : nonBomCharsets.entrySet()) {
            if (entry.getKey().toUpperCase()
                    .matches(".*UTF-16[\\s]*-?[\\s]*\\(?[BL]E\\)?.*")) {
                contexts.addAll(entry.getValue());
            }
        }
        if (!contexts.isEmpty()) {
            assertions.add(AssertionProvider.getForWith(
                    "rep_charset_utf16lebe", Assertion.Level.ERROR, contexts));
        }
    }

    /**
     * Charset report: XML Declaration used. Context has the original XML
     * declaration ('?xml' tag) verbatim.
     */
    private void addAssertionRepCharsetXmlDeclUsed() {
        if (parsedDocument.getCharsetXmlDeclaration() != null) {
            if (parsedDocument.isHtml()
                    || parsedDocument.isHtml5()
                    && !parsedDocument.isServedAsXml()) {
                assertions.add(AssertionProvider.getForWith(
                        "rep_charset_xml_decl_used", Assertion.Level.ERROR,
                        Arrays.asList(parsedDocument.getXmlDeclaration())));
            } else if (parsedDocument.isXhtml10()
                    && !parsedDocument.isServedAsXml()) {
                assertions.add(AssertionProvider.getForWith(
                        "rep_charset_xml_decl_used", Assertion.Level.WARNING,
                        Arrays.asList(parsedDocument.getXmlDeclaration())));
            }
        }
    }

    /**
     * Report languages: A 'lang' attribute value did not match an 'xml:lang'
     * value when they appeared together on the same tag. Context has a list of
     * the problem tags verbatim, each accompanied by the conflicting language
     * declarations.
     */
    private void addAssertionRepLangConflict() {
        if (!parsedDocument.getAllConflictingLangAttributes().isEmpty()) {
            ArrayList<String> contexts = new ArrayList<>();
            for (List<String> conflict
                    : parsedDocument.getAllConflictingLangAttributes()) {
                contexts.add(conflict.toString());
            }
            assertions.add(AssertionProvider.getForWith(
                    "rep_lang_conflict", Assertion.Level.ERROR, contexts));
        }
    }

    /**
     * Report languages: Content-Language meta element. Context has the 'meta'
     * tag verbatim.
     */
    private void addAssertionRepLangContentLangMeta() {
        if (parsedDocument.getLangMeta() != null) {
            assertions.add(AssertionProvider.getForWith(
                    "rep_lang_content_lang_meta",
                    parsedDocument.isHtml5()
                    ? Assertion.Level.ERROR : Assertion.Level.WARNING,
                    Arrays.asList(parsedDocument.getLangMeta())));
        }
    }

    /**
     * Report languages: The 'html' tag has no effective language declaration.
     * Context has the opening 'html' tag verbatim.
     */
    private void addAssertionRepLangHtmlNoEffectiveLang() {
        if (parsedDocument.isServedAsXml()
                && parsedDocument.getOpeningHtmlTagLang() != null
                && parsedDocument.getOpeningHtmlTagXmlLang() == null) {
            assertions.add(AssertionProvider.getForWith(
                    "rep_lang_html_no_effective_lang_xml",
                    Assertion.Level.WARNING,
                    Arrays.asList(parsedDocument.getOpeningHtmlTag())));
        } else if (!parsedDocument.isServedAsXml()
                && parsedDocument.getOpeningHtmlTagLang() == null
                && parsedDocument.getOpeningHtmlTagXmlLang() != null) {
            if (parsedDocument.isHtml()
                    || parsedDocument.isHtml5()
                    || (parsedDocument.isXhtml10()
                    && !parsedDocument.isServedAsXml())) {
                assertions.add(AssertionProvider.getForWith(
                        "rep_lang_html_no_effective_lang_html",
                        Assertion.Level.WARNING,
                        Arrays.asList(parsedDocument.getOpeningHtmlTag())));
            } else {
                assertions.add(AssertionProvider.getForWith(
                        "rep_lang_html_no_effective_lang",
                        Assertion.Level.WARNING,
                        Arrays.asList(parsedDocument.getOpeningHtmlTag())));
            }
        }
    }

    /**
     * Report languages: A language attribute value was incorrectly formed.
     * Context has the bad values verbatim.
     */
    private void addAssertionRepLangMalformedAttr() {
        Set<String> attrs = new TreeSet<>();
        attrs.addAll(parsedDocument.getAllLangAttributes());
        attrs.addAll(parsedDocument.getAllXmlLangAttributes());
        Set<String> contexts = new TreeSet<>();
        for (String atttr : attrs) {
            // TODO Review this regexp. BCP47, RFC 5646, RFC 4647.
            if (!atttr.matches("[a-zA-Z0-9]{1,8}(-[a-zA-Z0-9]{1,8})*")) {
                contexts.add(atttr);
            }
        }
        if (!contexts.isEmpty()) {
            assertions.add(AssertionProvider.getForWith(
                    "rep_lang_malformed_attr",
                    Assertion.Level.ERROR,
                    new ArrayList<>(contexts)));
        }
    }

    /**
     * Report languages: A tag uses an 'xml:lang' attribute without an
     * associated 'lang' attribute. Context is empty.
     */
    private void addAssertionRepLangMissingHtmlAttr() {
        if ((parsedDocument.isXhtml10() & !parsedDocument.isServedAsXml())
                || parsedDocument.isHtml5()) {
            if (!parsedDocument.getAllLangAttributeTags()
                    .containsAll(
                    parsedDocument.getAllXmlLangAttributeTags())) {
                // TODO: Needs some contexts.
                assertions.add(AssertionProvider.getForWith(
                        "rep_lang_missing_html_attr",
                        parsedDocument.isHtml5()
                        ? Assertion.Level.ERROR : Assertion.Level.WARNING,
                        new ArrayList<String>()));
            }
        }
    }

    /**
     * Report languages: A tag uses an 'lang' attribute without an associated
     * 'xml:lang' attribute. Context is empty.
     */
    private void addAssertionRepLangMissingXmlAttr() {
        if (parsedDocument.isXhtml10() || parsedDocument.isXhtml11()) {
            if (!parsedDocument.getAllXmlLangAttributeTags()
                    .containsAll(
                    parsedDocument.getAllLangAttributeTags())) {
                // TODO: Needs some contexts.
                assertions.add(AssertionProvider.getForWith(
                        "rep_lang_missing_xml_attr",
                        parsedDocument.isServedAsXml()
                        ? Assertion.Level.ERROR : Assertion.Level.WARNING,
                        new ArrayList<String>()));
            }
        }
    }

    /**
     * Report languages: The html tag has no language attribute. Context has the
     * opening 'html' tag verbatim.
     */
    private void addAssertionRepLangNoLangAttr() {
        if (parsedDocument.getOpeningHtmlTagLang() == null
                && parsedDocument.getOpeningHtmlTagXmlLang() == null) {
            assertions.add(AssertionProvider.getForWith(
                    parsedDocument.isHtml() || parsedDocument.isHtml5()
                    ? "rep_lang_no_lang_attr_html"
                    : parsedDocument.isXhtml10()
                    && !parsedDocument.isServedAsXml()
                    ? "rep_lang_no_lang_attr_xml"
                    : "rep_lang_no_lang_attr",
                    Assertion.Level.WARNING,
                    Arrays.asList(
                    parsedDocument.getOpeningHtmlTag())));
        }
    }

    /**
     * Report languages: This HTML file contains xml:lang attributes. Context is
     * empty.
     */
    private void addAssertionRepLangXmlAttrInHtml() {
        if (parsedDocument.isHtml()
                && !parsedDocument.getAllXmlLangAttributes().isEmpty()) {
            assertions.add(AssertionProvider.getForWith(
                    "rep_lang_xml_attr_in_html", Assertion.Level.ERROR,
                    // TODO: Needs some contexts.
                    new ArrayList<String>()));
        }
    }

    /**
     * Markup report: There are non-NFC class or id names. Context has a set of
     * the problem names followed by a list of the problem tags verbatim.
     */
    private void addAssertionRepLatinNonNfc() {
        if (!parsedDocument.getAllNonNfcClassIdNames().isEmpty()) {
            List<String> contexts = new ArrayList<>();
            contexts.addAll(parsedDocument.getAllNonNfcClassIdNames());
            contexts.addAll(parsedDocument.getAllNonNfcClassIdTags());
            assertions.add(AssertionProvider.getForWith(
                    "rep_latin_non_nfc", Assertion.Level.WARNING, contexts));
        }
    }

    /**
     * Markup report: Document contains a 'bdo' tag with no 'dir' attribute.
     * Context has a list of problem 'bdo' tags verbatim.
     */
    private void addAssertionRepMarkupBdoNoDir() {
        if (!parsedDocument.getBdoTagsWithoutDir().isEmpty()) {
            assertions.add(AssertionProvider.getForWith(
                    "rep_markup_bdo_no_dir", Assertion.Level.INFO,
                    parsedDocument.getBdoTagsWithoutDir()));
        }
    }

    /**
     * Markup report: Tags in the document have a 'dir' attribute with an
     * incorrect value (should be "rtl", "ltr", or, except in HTML5, "auto").
     * Context has a set of the incorrect values used in the document.
     */
    private void addAssertionRepMarkupDirIncorrect() {
        if (!parsedDocument.getAllDirAttributes().isEmpty()) {
            Set<String> contexts = new TreeSet<>();
            for (String attribute : parsedDocument.getAllDirAttributes()) {
                if (!attribute.trim().equalsIgnoreCase("rtl")
                        && !attribute.trim().equalsIgnoreCase("ltr")
                        || (!parsedDocument.isHtml5()
                        && attribute.trim().equalsIgnoreCase("auto"))) {
                    contexts.add(attribute);
                }
            }
            if (!contexts.isEmpty()) {
                assertions.add(AssertionProvider.getForWith(
                        "rep_markup_dir_incorrect", Assertion.Level.ERROR,
                        /* TODO: This context would be more helpful if it also
                         * provided the origins of the declarations. */
                        new ArrayList<>(contexts)));
            }
        }
    }

// rep_markup_tags_no_class (INFO)
// "INFO: <b> tags found in source"
// "INFO: <i> tags found in source"
    /**
     * Markup report: 'b' or 'i' tags found with no 'class' attribute. Context
     * has a list of the problem tags verbatim.
     */
    private void addAssertionRepMarkupTagsNoClass() {
        if (!parsedDocument.getbITagsWithoutClass().isEmpty()) {
            assertions.add(AssertionProvider.getForWith(
                    "rep_markup_tags_no_class", Assertion.Level.INFO,
                    parsedDocument.getbITagsWithoutClass()));
        }
    }
}
