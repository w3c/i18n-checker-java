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
                    new ArrayList()));
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
                    Arrays.asList(
                    parsedDocument.getCharsetHttp(),
                    parsedDocument.getContentType())));
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

    // rep_charset_bom_found (WARNING)
    // "CHARSET REPORT: UTF-8 BOM found at start of file"
    private void addAssertionRepCharsetBomFound() {
        if (parsedDocument.getByteOrderMark() != null
                && parsedDocument.getCharsetByteOrderMark().trim()
                .toLowerCase().contains("utf-8")) {
            assertions.add(new Assertion(
                    "rep_charset_bom_found",
                    Assertion.Level.WARNING,
                    "UTF-8 BOM found at start of file",
                    "Using an editor or an appropriate tool, remove the byte"
                    + " order mark from the beginning of the file. This can"
                    + " often be achieved by saving the document with the"
                    + " appropriate settings in the editor. On the other hand,"
                    + " some editors (such as Notepad on Windows) do not give"
                    + " you a choice, and always add the byte order mark. In"
                    + " this case you may need to use a different editor.",
                    Arrays.asList(
                    parsedDocument.getByteOrderMark().toString())));
        }
    }

    // rep_charset_bom_in_content (WARNING)
    // "CHARSET REPORT: BOM in content"
    private void addAssertionRepCharsetBomInContent() {
        if (!parsedDocument.getBomsInContent().isEmpty()) {
            assertions.add(new Assertion(
                    "rep_charset_bom_in_content",
                    Assertion.Level.WARNING,
                    "BOM found in content",
                    "Using an editor or an appropriate tool, remove the byte"
                    + " order mark from the beginning of the file or chunk of"
                    + " content where it appears. If the problem does arise"
                    + " from a BOM at the top of an included file, this can"
                    + " often be achieved by saving the content with"
                    + " appropriate settings in the editor. On the other hand,"
                    + " some editors (such as Notepad on Windows) do not give"
                    + " you a choice, and always add the byte order mark. In"
                    + " this case you may need to use a different editor.",
                    parsedDocument.getBomsInContent()));
        }
    }

    // rep_charset_charset_attr (ERROR)
    // rep_charset_charset_attr (WARNING)
    // "CHARSET REPORT: charset attribute used on a or link elements"
    private void addAssertionRepCharsetCharsetAttr() {
        if (!parsedDocument.getCharsetLinkTags().isEmpty()) {
            assertions.add(new Assertion(
                    "rep_charset_charset_attr",
                    parsedDocument.isHtml5()
                    ? Assertion.Level.ERROR : Assertion.Level.WARNING,
                    "<code>charset</code> attribute used on <code"
                    + ">a</code> or <code>link</code>"
                    + " elements",
                    "Remove the charset attribute. If pointing to a page that"
                    + " is under your control, ensure that any appropriate"
                    + " character encoding information is provided for that"
                    + " page.",
                    parsedDocument.getCharsetLinkTags()));
        }
    }

    // rep_charset_conflict (ERROR)
    // "CHARSET REPORT: Conflicting character encoding declarations"
    private void addAssertionRepCharsetConflict() {
        if (parsedDocument.getAllCharsetDeclarations().size() > 1) {
            assertions.add(new Assertion(
                    "rep_charset_conflict",
                    Assertion.Level.ERROR,
                    "Conflicting character encoding declarations",
                    "Change the character encoding declarations so that they"
                    + " match.  Ensure that your document is actually saved in"
                    + " the encoding you choose.",
                    new ArrayList<>(
                    parsedDocument.getAllCharsetDeclarations())));
        }
    }

    // rep_charset_incorrect_use_meta (ERROR)
    // rep_charset_incorrect_use_meta (WARNING)
    // "CHARSET REPORT: Incorrect use of meta encoding declaration"
    private void addAssertionRepCharsetIncorrectUseMeta() {
        if (!parsedDocument.getCharsetMetaTags().isEmpty()
                && parsedDocument.getCharsetHttp() == null
                && parsedDocument.getByteOrderMark() == null
                && parsedDocument.getCharsetXmlDeclaration() == null
                && parsedDocument.isXhtml1X()
                && !parsedDocument.getCharsetMetaTags()
                .containsKey("utf-8")
                && !parsedDocument.getCharsetMetaTags()
                .containsKey("utf-16")) {
            List<String> contexts = new ArrayList<>();
            for (List<String> list : parsedDocument.getCharsetMetaTags()
                    .values()) {
                contexts.addAll(list);
            }
            if (parsedDocument.isServedAsXml()) {
                assertions.add(new Assertion(
                        "rep_charset_incorrect_use_meta",
                        Assertion.Level.ERROR,
                        "Incorrect use of <code>meta</code>"
                        + " encoding declarations",
                        "Add an XML declaration with encoding information,"
                        + " or change the character encoding for this page"
                        + " to UTF-8. If this page is never parsed as HTML,"
                        + " you can remove the <code>meta</code>"
                        + " tag.",
                        contexts));
            } else {
                assertions.add(new Assertion(
                        "rep_charset_incorrect_use_meta",
                        Assertion.Level.WARNING,
                        "Incorrect use of <code>meta</code>"
                        + " encoding declarations",
                        "There is no problem for this XHTML document as"
                        + " long as it is being served as HTML (text/html)."
                        + " If, however, you expect it to be processed as"
                        + " XML at some point, you should either add an XML"
                        + " declaration with encoding information, or use"
                        + " UTF-8 as the character encoding of your page.",
                        contexts));
            }
        }
    }

    // rep_charset_meta_charset_invalid (WARNING)
    // "CHARSET REPORT: Meta charset tag will cause validation to fail"
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
            assertions.add(new Assertion(
                    "rep_charset_meta_charset_invalid",
                    Assertion.Level.WARNING,
                    "A <code>meta</code> tag with a <code"
                    + ">charset</code> attribute will cause"
                    + " validation to fail",
                    "If you want this page to be valid HTML, replace the <code"
                    + ">charset</code> attribute with <code"
                    + ">http-equiv</code> and <code"
                    + ">content</code> attributes, eg."
                    + " <code>&lt;meta http-equiv='Content-Type'"
                    + " content='text/html; charset=utf-8'&gt;</code>.",
                    contexts));
        }
    }

    // rep_charset_meta_ineffective (INFO)
    // "CHARSET REPORT: Meta encoding declarations don't work with XML"
    private void addAssertionRepCharsetMetaIneffective() {
        if (!parsedDocument.getCharsetMetaTags().isEmpty()
                && parsedDocument.isServedAsXml()) {
            List<String> contexts = new ArrayList<>();
            for (List<String> list : parsedDocument.getCharsetMetaTags()
                    .values()) {
                contexts.addAll(list);
            }
            assertions.add(new Assertion(
                    "rep_charset_meta_ineffective",
                    Assertion.Level.INFO,
                    "<code>meta</code> encoding declarations don't"
                    + " work with XML",
                    "Unless you sometimes serve this page as <code"
                    + ">text/html</code>, remove the <code"
                    + ">meta</code> tag and ensure you have an XML"
                    + " declaration with encoding information.",
                    contexts));
        }
    }

    // rep_charset_multiple_meta (ERROR)
    // "CHARSET REPORT: Multiple encoding declarations using the meta tag"
    private void addAssertionRepCharsetMultipleMeta() {
        if (parsedDocument.getCharsetMetaTags().size() > 1) {
            List<String> contexts = new ArrayList<>();
            for (List<String> list : parsedDocument.getCharsetMetaTags()
                    .values()) {
                contexts.addAll(list);
            }
            assertions.add(new Assertion(
                    "rep_charset_multiple_meta",
                    Assertion.Level.ERROR,
                    "Multiple encoding declarations using the <code"
                    + ">meta</code> tag",
                    "Edit the markup to remove all but one <code"
                    + ">meta</code> element.",
                    contexts));
        }
    }

    // rep_charset_no_effective_charset (WARNING)
    // "CHARSET REPORT: No effective character encoding information"
    private void addAssertionRepCharsetNoEffectiveCharset() {
        if (parsedDocument.getCharsetXmlDeclaration() != null
                && parsedDocument.getCharsetHttp() == null
                && parsedDocument.getByteOrderMark() == null
                && parsedDocument.getCharsetMetaTags().isEmpty()
                && (parsedDocument.isHtml()
                || parsedDocument.isHtml5()
                || (parsedDocument.isXhtml10()
                && !parsedDocument.isServedAsXml()))) {
            assertions.add(new Assertion(
                    "rep_charset_no_effective_charset",
                    Assertion.Level.WARNING,
                    "No effective character encoding information",
                    "Add a <code>meta</code> element to indicate"
                    + " the character encoding of the page. You could also"
                    + " declare the encoding in the HTTP header, but it is"
                    + " recommended that you always use a <code"
                    + ">meta</code> element too.",
                    new ArrayList<String>()));
        }
    }

    // rep_charset_no_encoding_xml (WARNING)
    // (See also 'rep_charset_none'.)
    // CHARSET REPORT: "No character encoding information"
    private void addAssertionRepCharsetNoEncodingXml() {
        if (parsedDocument.getAllCharsetDeclarations().isEmpty()
                && parsedDocument.isServedAsXml()) {
            assertions.add(new Assertion(
                    "rep_charset_no_encoding_xml",
                    Assertion.Level.WARNING,
                    "No in-document encoding declaration found",
                    "Add information to indicate the character encoding of the"
                    + " page inside the page itself.",
                    new ArrayList<String>()));
        }
    }

    // rep_charset_no_in_doc (WARNING)
    // "CHARSET REPORT: No charset declaration in the document"
    private void addAssertionRepCharsetNoInDoc() {
        if (parsedDocument.getInDocCharsetDeclarations().isEmpty()
                && !parsedDocument.getAllCharsetDeclarations().isEmpty()) {
            assertions.add(new Assertion(
                    "rep_charset_no_in_doc",
                    Assertion.Level.WARNING,
                    "Encoding declared only in HTTP header",
                    "Add information to indicate the character encoding of the"
                    + " page inside the page itself.",
                    new ArrayList<String>()));
        }
    }

    // rep_charset_no_utf8 (INFO)
    // "CHARSET REPORT: Non-UTF8 character encoding declared"
    private void addAssertionRepCharsetNoUtf8() {
        if (!parsedDocument.getNonUtf8CharsetDeclarations().isEmpty()) {
            assertions.add(new Assertion(
                    "rep_charset_no_utf8",
                    Assertion.Level.INFO,
                    "Non-UTF-8 character encoding declared",
                    "Set your authoring tool to save your content as UTF-8, and"
                    + " change the encoding declarations.",
                    new ArrayList(
                    parsedDocument.getNonUtf8CharsetDeclarations())));
        }
    }

    // rep_charset_no_visible_charset (WARNING)
    // "CHARSET REPORT: No visible in-document encoding specified"
    private void addAssertionRepCharsetNoVisibleCharset() {
        if (parsedDocument.getByteOrderMark() != null
                && parsedDocument.getCharsetXmlDeclaration() == null
                && parsedDocument.getCharsetMetaTags().isEmpty()) {
            if (parsedDocument.getCharsetByteOrderMark()
                    .equals("UTF-8")) {
                assertions.add(new Assertion(
                        "rep_charset_no_visible_charset",
                        Assertion.Level.WARNING,
                        "No visible in-document encoding declared",
                        "Add a <code>meta</code> tag or XML"
                        + " declaration, as appropriate, to your page to"
                        + " indicate the character encoding used.",
                        new ArrayList<String>()));
            }
        }
    }

    // rep_charset_none (ERROR)
    // rep_charset_none (WARNING)
    // (See also 'rep_charset_no_encoding_xml'.)
    // CHARSET REPORT: "No character encoding information"
    private void addAssertionRepCharsetNone() {
        if (parsedDocument.getAllCharsetDeclarations().isEmpty()) {
            if (!parsedDocument.isServedAsXml()) {
                Assertion.Level level = parsedDocument.isHtml5()
                        ? Assertion.Level.ERROR : Assertion.Level.WARNING;
                assertions.add(new Assertion(
                        "rep_charset_none",
                        level,
                        "No character encoding information",
                        "Add information to indicate the character encoding of"
                        + " the page.",
                        new ArrayList<String>()));
            }
        }
    }

    // rep_charset_pragma (INFO)
    // "CHARSET REPORT: Meta charset declaration uses http-equiv"
    private void addAssertionRepCharsetPragma() {
        if (parsedDocument.isHtml5()) {
            List<String> contexts = new ArrayList<>();
            for (List<String> list : parsedDocument
                    .getCharsetMetaTags().values()) {
                for (String context : list) {
                    if (context.contains("http-equiv")) {
                        contexts.add(context);
                    }
                }
            }
            if (!contexts.isEmpty()) {
                assertions.add(new Assertion(
                        "rep_charset_pragma",
                        Assertion.Level.INFO,
                        "<code>meta</code> character encoding"
                        + " declaration uses <code"
                        + ">http-equiv</code>",
                        "Replace the <code>http-equiv</code> and"
                        + " <code>content</code> attributes in your"
                        + " <code>meta</code> tag with a <code"
                        + ">charset</code> attribute.",
                        contexts));
            }
        }
    }

    // rep_charset_utf16_meta (ERROR)
    // "CHARSET REPORT: Meta character encoding declaration used in UTF-16 page"
    private void addAssertionRepCharsetUtf16Meta() {
        if (parsedDocument.getCharsetMetaTags().containsKey("utf-16")
                && parsedDocument.isUtf16() && parsedDocument.isHtml5()) {
            assertions.add(new Assertion(
                    "rep_charset_utf16_meta",
                    Assertion.Level.ERROR,
                    "Meta character encoding declaration used in UTF-16"
                    + " page",
                    "Remove the <code>meta</code> encoding"
                    + " declaration.",
                    parsedDocument.getCharsetMetaTags().get("utf-16")));
        }
    }

    // rep_charset_utf16lebe (ERROR)
    // "CHARSET REPORT: UTF-16LE or UTF-16BE found in a character encoding ..."
    private void addAssertionRepCharsetUtf16lebe() {
        Map<String, List<String>> nonBomCharsets = new TreeMap<>();
        List<String> contexts = new ArrayList<>();
        if (parsedDocument.getCharsetHttp() != null) {
            nonBomCharsets.put(parsedDocument.getCharsetHttp(),
                    Arrays.asList(parsedDocument.getContentType()));
        }
        if (parsedDocument.getCharsetXmlDeclaration() != null) {
            nonBomCharsets.put(parsedDocument.getCharsetXmlDeclaration(),
                    Arrays.asList(parsedDocument.getXmlDeclaration()));
        }
        nonBomCharsets.putAll(parsedDocument.getCharsetMetaTags());
        for (Map.Entry<String, List<String>> entry
                : nonBomCharsets.entrySet()) {
            if (entry.getKey().matches("utf-16 ?\\(?[bl]e\\)?")) {
                for (List<String> list : nonBomCharsets.values()) {
                    contexts.addAll(list);
                }
            }
        }
        if (!contexts.isEmpty()) {
            assertions.add(new Assertion(
                    "rep_charset_utf16lebe",
                    Assertion.Level.ERROR,
                    "UTF-16LE or UTF-16BE found in a character encoding"
                    + " declaration",
                    "Ensure that the page starts with a byte-order mark (BOM)"
                    + " and change the encoding declaration(s) to"
                    + " \\\"UTF-16\\\".",
                    contexts));
        }
    }

    // rep_charset_xml_decl_used (ERROR)
    // rep_charset_xml_decl_used (WARNING)
    // "CHARSET REPORT: XML Declaration used"
    private void addAssertionRepCharsetXmlDeclUsed() {
        if (parsedDocument.getCharsetXmlDeclaration() != null) {
            if (parsedDocument.isHtml()
                    || parsedDocument.isHtml5()
                    && !parsedDocument.isServedAsXml()) {
                assertions.add(new Assertion(
                        "rep_charset_xml_decl_used",
                        Assertion.Level.ERROR,
                        "XML declaration used",
                        "Remove the XML declaration from your page. Use a <code"
                        + ">meta</code> element instead to declare"
                        + " the character encoding of the page.",
                        Arrays.asList(parsedDocument.getXmlDeclaration())));
            } else if (parsedDocument.isXhtml10()
                    && !parsedDocument.isServedAsXml()) {
                assertions.add(new Assertion(
                        "rep_charset_xml_decl_used",
                        Assertion.Level.WARNING,
                        "XML declaration used",
                        "Since you are using XHTML 1.x but serving it as"
                        + " text/html, use UTF-8 for your page and remove the"
                        + " XML declaration.",
                        Arrays.asList(parsedDocument.getXmlDeclaration())));
            }
        }
    }

    // rep_lang_conflict (ERROR)
    // "ERROR: A lang attribute value did not match an xml:lang value when ..."
    private void addAssertionRepLangConflict() {
        if (!parsedDocument.getAllConflictingLangAttributes().isEmpty()) {
            ArrayList<String> contexts = new ArrayList<>();
            for (List<String> conflict
                    : parsedDocument.getAllConflictingLangAttributes()) {
                contexts.add(conflict.toString());
            }
            assertions.add(new Assertion(
                    "rep_lang_conflict",
                    Assertion.Level.ERROR,
                    "A <code>lang</code> attribute value did not"
                    + " match an <code>xml:lang</code> value when"
                    + " they appeared together on the same tag.",
                    "Change one of the values in each tag by editing the"
                    + " markup",
                    contexts));
        }
    }

    // rep_lang_content_lang_meta (ERROR)
    // rep_lang_content_lang_meta (WARNING)
    // "LANG REPORT: Content-Language meta element"
    private void addAssertionRepLangContentLangMeta() {
        if (parsedDocument.getLangMeta() != null) {
            assertions.add(new Assertion(
                    "rep_lang_content_lang_meta",
                    parsedDocument.isHtml5()
                    ? Assertion.Level.ERROR : Assertion.Level.WARNING,
                    "Content-Language <code>meta</code> element used"
                    + " to set the default document language",
                    "Remove the Content-Language meta element, and ensure that"
                    + " you have used an attribute on the <code"
                    + ">html</code> tag to specify the default"
                    + " language of the page.",
                    Arrays.asList(parsedDocument.getLangMeta())));
        }
    }

    // rep_lang_html_no_effective_lang (WARNING)
    // "WARNING: The html tag has no effective language declaration"
    private void addAssertionRepLangHtmlNoEffectiveLang() {
        if (parsedDocument.isServedAsXml()
                && parsedDocument.getOpeningHtmlTagLang() != null
                && parsedDocument.getOpeningHtmlTagXmlLang() == null) {
            assertions.add(new Assertion(
                    "rep_lang_html_no_effective_lang",
                    Assertion.Level.WARNING,
                    "The language declaration in the <code"
                    + ">html</code> tag will have no effect ",
                    "Since this page is served as XML, use the <code"
                    + ">xml:lang</code> attribute instead of a"
                    + " <code>lang</code> attribute. If there is a"
                    + " chance that this page will also be served as <code"
                    + ">text/html</code> in some circumstances, use"
                    + " both.",
                    Arrays.asList(parsedDocument.getOpeningHtmlTag())));
        } else if (!parsedDocument.isServedAsXml()
                && parsedDocument.getOpeningHtmlTagLang() == null
                && parsedDocument.getOpeningHtmlTagXmlLang() != null) {
            String description = parsedDocument.isHtml()
                    || parsedDocument.isHtml5()
                    || (parsedDocument.isXhtml10()
                    && !parsedDocument.isServedAsXml())
                    ? "Since this page is served as HTML, use the <code"
                    + ">lang</code> attribute. If there is a chance"
                    + " that the same page will also be processed by an XML"
                    + " parser, use both the <code>lang</code>"
                    + " attribute and the <code>xml:lang</code>"
                    + " attribute."
                    : "Since this page is served as HTML, use the <code"
                    + ">lang</code> attribute.";
            assertions.add(new Assertion(
                    "rep_lang_html_no_effective_lang",
                    Assertion.Level.WARNING,
                    "The language declaration in the <code"
                    + ">html</code> tag will have no effect ",
                    description,
                    Arrays.asList(parsedDocument.getOpeningHtmlTag())));
        }
    }

    // rep_lang_malformed_attr (ERROR)
    // "WARNING: A language attribute value was incorrectly formed."
    private void addAssertionRepLangMalformedAttr() {
        Set<String> attrs = new TreeSet<>();
        attrs.addAll(parsedDocument.getAllLangAttributes());
        attrs.addAll(parsedDocument.getAllXmlLangAttributes());
        Set<String> malformedAttrs = new TreeSet<>();
        for (String atttr : attrs) {
            // TODO Review this regexp. RFC 5646, RFC 4647
            if (!atttr.matches("[a-zA-Z0-9]{1,8}(-[a-zA-Z0-9]{1,8})*")) {
                malformedAttrs.add(atttr);
            }
        }
        if (!malformedAttrs.isEmpty()) {
            assertions.add(new Assertion(
                    "rep_lang_malformed_attr",
                    Assertion.Level.ERROR,
                    "A language attribute value was incorrectly formed",
                    "Change the attribute values to conform to BCP47 syntax"
                    + " rules.",
                    new ArrayList<>(malformedAttrs)));
        }
    }

    // rep_lang_missing_html_attr (ERROR)
    // rep_lang_missing_html_attr (WARNING)
    // "WARNING: A tag uses an xml:lang attribute without an associated ..."
    private void addAssertionRepLangMissingHtmlAttr() {
        if ((parsedDocument.isXhtml10() & !parsedDocument.isServedAsXml())
                || parsedDocument.isHtml5()) {
            if (!parsedDocument.getAllLangAttributeTags()
                    .containsAll(
                    parsedDocument.getAllXmlLangAttributeTags())) {
                assertions.add(new Assertion(
                        "rep_lang_missing_html_attr",
                        parsedDocument.isHtml5()
                        ? Assertion.Level.ERROR : Assertion.Level.WARNING,
                        "A tag uses an <code>xml:lang</code>"
                        + " attribute without an associated <code"
                        + ">lang</code> attribute",
                        "Add a <code>lang</code> attribute to each"
                        + " of the above tags, with the same value as the <code"
                        + ">xml:lang</code> attribute.",
                        new ArrayList<String>()));
            }
        }
    }

    // rep_lang_missing_xml_attr (ERROR)
    // rep_lang_missing_xml_attr (WARNING)
    // "WARNING: A tag uses a lang attribute without an associated xml:lang ..."
    private void addAssertionRepLangMissingXmlAttr() {
        if (parsedDocument.isXhtml10() || parsedDocument.isXhtml11()) {
            if (!parsedDocument.getAllXmlLangAttributeTags()
                    .containsAll(
                    parsedDocument.getAllLangAttributeTags())) {
                assertions.add(new Assertion(
                        "rep_lang_missing_xml_attr",
                        parsedDocument.isServedAsXml()
                        ? Assertion.Level.ERROR : Assertion.Level.WARNING,
                        "A tag uses a <code>lang</code> attribute"
                        + " without an associated <code"
                        + ">xml:lang</code> attribute",
                        "Add an <code>xml:lang</code> attribute to"
                        + " each of the above tags, with the same value as the"
                        + " <code>lang</code> attribute.",
                        new ArrayList<String>()));
            }
        }
    }

    // rep_lang_no_lang_attr (WARNING)
    // "WARNING: The html tag has no language attribute"
    private void addAssertionRepLangNoLangAttr() {
        if (parsedDocument.getOpeningHtmlTagLang() == null
                && parsedDocument.getOpeningHtmlTagXmlLang() == null) {
            String description =
                    parsedDocument.isHtml() || parsedDocument.isHtml5()
                    ? "Add a <code>lang</code> attribute that"
                    + " indicates the default language of your page. Example:"
                    + " <code>lang='de'</code>"
                    : parsedDocument.isXhtml10()
                    && !parsedDocument.isServedAsXml()
                    ? "Since this is an XHTML page served as HTML, add both a"
                    + " <code>lang</code> attribute and an <code"
                    + ">xml:lang</code> attribute to the html tag to"
                    + " indicate the default language of your page.  The <code"
                    + ">lang</code> attribute is understood by HTML"
                    + " processors, but not by XML processors, and vice versa."
                    + " Example: <code>lang=&quot;de&quot;"
                    + " xml:lang=&quot;de&quot;</code>"
                    : "Add an <code>xml:lang</code> attribute that"
                    + " indicates the default language of your page. Example:"
                    + " <code>xml:lang='de'</code>";
            assertions.add(new Assertion(
                    "rep_lang_no_lang_attr",
                    Assertion.Level.WARNING,
                    "The <code>html</code> tag has no"
                    + " language attribute",
                    description,
                    Arrays.asList(
                    parsedDocument.getOpeningHtmlTag())));

        }
    }

    // rep_lang_xml_attr_in_html (ERROR)
    // "WARNING: This HTML file contains xml:lang attributes"
    private void addAssertionRepLangXmlAttrInHtml() {
        if (parsedDocument.isHtml()
                && !parsedDocument.getAllXmlLangAttributes().isEmpty()) {
            assertions.add(new Assertion(
                    "rep_lang_xml_attr_in_html",
                    Assertion.Level.ERROR,
                    "This HTML file contains <code>xml:lang</code>"
                    + " attributes",
                    "Remove the <code>xml:lang</code> attributes"
                    + " from the markup, replacing them, where appropriate,"
                    + " with <code>lang</code> attributes.",
                    new ArrayList<String>()));
        }
    }

    // rep_latin_non_nfc (WARNING)
    // "WARNING: are there non-NFC class or id names?"
    private void addAssertionRepLatinNonNfc() {
        if (!parsedDocument.getAllNonNfcClassIdNames().isEmpty()) {
            List<String> contexts = new ArrayList<>();
            contexts.addAll(parsedDocument.getAllNonNfcClassIdNames());
            contexts.addAll(parsedDocument.getAllNonNfcClassIdTags());
            assertions.add(new Assertion(
                    "rep_latin_non_nfc",
                    Assertion.Level.WARNING,
                    "Class or id names found that are not in Unicode"
                    + " Normalization&nbsp;Form&nbsp;C",
                    "It is recommended to save all content as Unicode"
                    + " Normalization Form C (NFC).",
                    contexts));
        }
    }

    // rep_markup_bdo_no_dir
    // "ERROR: <bdo> tag without dir"
    private void addAssertionRepMarkupBdoNoDir() {
        if (!parsedDocument.getBdoTagsWithoutDir().isEmpty()) {
            assertions.add(new Assertion(
                    "rep_markup_bdo_no_dir",
                    Assertion.Level.INFO,
                    "<code>bdo</code> tags found with no"
                    + " <code>dir</code> attribute",
                    "Add a <code>dir</code> attribute to each <code"
                    + ">bdo</code> tag.",
                    parsedDocument.getBdoTagsWithoutDir()));
        }
    }

    // rep_markup_dir_incorrect (ERROR)
    // "ERROR: Incorrect values used for dir attribute"
    private void addAssertionRepMarkupDirIncorrect() {
        if (!parsedDocument.getAllDirAttributes().isEmpty()) {
            Set<String> incorrectDirs = new TreeSet<>();
            for (String attribute : parsedDocument.getAllDirAttributes()) {
                if (!attribute.equalsIgnoreCase("rtl")
                        && !attribute.equalsIgnoreCase("ltr")
                        || (!parsedDocument.isHtml5()
                        && attribute.equalsIgnoreCase("auto"))) {
                    incorrectDirs.add(attribute);
                }
            }
            if (!incorrectDirs.isEmpty()) {
                assertions.add(new Assertion(
                        "rep_markup_dir_incorrect",
                        Assertion.Level.ERROR,
                        "Incorrect values used for <code>dir</code>"
                        + " attribute",
                        "Correct the attribute values.",
                        new ArrayList<>(incorrectDirs)));
            }
        }
    }

    // rep_markup_tags_no_class (INFO)
    // "INFO: <b> tags found in source"
    // "INFO: <i> tags found in source"
    private void addAssertionRepMarkupTagsNoClass() {
        if (!parsedDocument.getbITagsWithoutClass().isEmpty()) {
            assertions.add(new Assertion(
                    "rep_markup_tags_no_class",
                    Assertion.Level.INFO,
                    "<code>b</code> or <code>i</code>"
                    + " tags found with no class attribute",
                    "You should not use <code>b</code> or <code>i</code> tags"
                    + " if there is a more descriptive and relevant tag"
                    + " available. If you do use them, it is usually better to"
                    + " add class attributes that describe the intended meaning"
                    + " of the markup, so that you can distinguish one use from"
                    + " another.",
                    parsedDocument.getbITagsWithoutClass()));
        }
    }
}
