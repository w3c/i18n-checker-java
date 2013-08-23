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
//        addAssertionCharsetReports();


        addAssertionRepCharset1024Limit();
        addAssertionRepCharsetBogusUtf16();
        addAssertionRepCharsetBogusUtf16();
        addAssertionRepCharsetBomFound();
        addAssertionRepCharsetBomInContent();
        addAssertionRepCharsetCharsetAttr();
        addAssertionRepCharsetCharsetAttr();
        addAssertionRepCharsetConflict();
        addAssertionRepCharsetIncorrectUseMeta();
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
        addAssertionRepCharsetNone();
        addAssertionRepCharsetPragma();
        addAssertionRepCharsetUtf16Meta();
        addAssertionRepCharsetUtf16lebe();
        addAssertionRepCharsetXmlDeclUsed();
        addAssertionRepCharsetXmlDeclUsed();
        addAssertionRepLangConflict();
        addAssertionRepLangContentLangMeta();
        addAssertionRepLangContentLangMeta();
        addAssertionRepLangHtmlNoEffectiveLang();
        addAssertionRepLangMalformedAttr();
        addAssertionRepLangMissingHtmlAttr();
        addAssertionRepLangMissingHtmlAttr();
        addAssertionRepLangMissingXmlAttr();
        addAssertionRepLangMissingXmlAttr();
        addAssertionRepLangNoLangAttr();
        addAssertionRepLangNoLangAttr();
        addAssertionRepLangXmlAttrInHtml();
        addAssertionRepLatinNonNfc();
        addAssertionRepMarkupBdoNoDir();
        addAssertionRepMarkupDirIncorrect();
        addAssertionRepMarkupDirIncorrect();
        addAssertionRepMarkupTagsNoClass();

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

    // rep_charset_1024_limit (ERROR)
    private void addAssertionRepCharset1024Limit() {
    }

    // rep_charset_bogus_utf16 (ERROR)
    // rep_charset_bogus_utf16 (INFO)
    private void addAssertionRepCharsetBogusUtf16() {
    }

    // rep_charset_bom_found (WARNING)
    // "CHARSET REPORT: UTF-8 BOM found at start of file"
    private void addAssertionRepCharsetBomFound() {
        if (parsedDocument.getByteOrderMark() != null
                && parsedDocument.getByteOrderMark().trim().toLowerCase()
                .contains("utf-8")) {
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
                    Arrays.asList(parsedDocument.getByteOrderMark())));
        }
    }

    // rep_charset_bom_in_content (WARNING)
    // "CHARSET REPORT: BOM in content"
    private void addAssertionRepCharsetBomInContent() {
        /* TODO: As far as I can tell, this is what the old version did, but I
         * don't think it's the correct behaviour. */
        if (parsedDocument.getByteOrderMark() != null) {
            assertions.add(new Assertion(
                    "rep_charset_bom_found",
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
                    Arrays.asList(parsedDocument.getByteOrderMark())));

        }
    }

    // rep_charset_charset_attr (ERROR)
    // rep_charset_charset_attr (WARNING)
    private void addAssertionRepCharsetCharsetAttr() {
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
        if (parsedDocument.getCharsetMeta() != null
                && parsedDocument.getCharsetHttp() == null
                && parsedDocument.getByteOrderMark() == null
                && parsedDocument.getCharsetXmlDeclaration() == null
                && parsedDocument.isXhtml1X()) {
            if (/* Note: These won't be short-circuited if they're moved the
                     * above 'if'. ~~~ Joe. */parsedDocument.getCharsetMeta()
                    .trim().toLowerCase()
                    .equals("utf-8")
                    || parsedDocument.getCharsetMeta().trim().toLowerCase()
                    .equals("utf-16")) {
                if (parsedDocument.isServedAsXml()) {
                    assertions.add(new Assertion(
                            "rep_charset_incorrect_use_meta",
                            Assertion.Level.ERROR,
                            "Incorrect use of <code class='kw'>meta</code>"
                            + " encoding declarations",
                            "Add an XML declaration with encoding information,"
                            + " or change the character encoding for this page"
                            + " to UTF-8. If this page is never parsed as HTML,"
                            + " you can remove the <code class='kw'>meta</code>"
                            + " tag.",
                            Arrays.asList(
                            parsedDocument.getCharsetMetaContext())));
                } else {

                    assertions.add(new Assertion(
                            "rep_charset_incorrect_use_meta",
                            Assertion.Level.WARNING,
                            "Incorrect use of <code class='kw'>meta</code>"
                            + " encoding declarations",
                            "There is no problem for this XHTML document as"
                            + " long as it is being served as HTML (text/html)."
                            + " If, however, you expect it to be processed as"
                            + " XML at some point, you should either add an XML"
                            + " declaration with encoding information, or use"
                            + " UTF-8 as the character encoding of your page.",
                            Arrays.asList(
                            parsedDocument.getCharsetMetaContext())));
                }
            }
        }
    }

    // rep_charset_meta_charset_invalid (WARNING)
    // "CHARSET REPORT: Meta charset tag will cause validation to fail"
    private void addAssertionRepCharsetMetaCharsetInvalid() {
        if (parsedDocument.isHtml5()
                && parsedDocument.getCharsetMeta() != null) {
            assertions.add(new Assertion(
                    "rep_charset_meta_charset_invalid",
                    Assertion.Level.WARNING,
                    "A <code class='kw'>meta</code> tag with a <code"
                    + " class='kw'>charset</code> attribute will cause"
                    + " validation to fail",
                    "If you want this page to be valid HTML, replace the <code"
                    + " class='kw'>charset</code> attribute with <code"
                    + " class='kw'>http-equiv</code> and <code"
                    + " class='kw'>content</code> attributes, eg."
                    + " <code>&lt;meta http-equiv='Content-Type'"
                    + " content='text/html; charset=utf-8'&gt;</code>.",
                    Arrays.asList(parsedDocument.getCharsetMeta(),
                    parsedDocument.getCharsetMetaContext())));
        }
    }

    // rep_charset_meta_ineffective (INFO)
    // "CHARSET REPORT: Meta encoding declarations don't work with XML"
    private void addAssertionRepCharsetMetaIneffective() {
        if (parsedDocument.getCharsetMeta() != null
                && parsedDocument.isServedAsXml()) {
            assertions.add(new Assertion(
                    "rep_charset_meta_ineffective",
                    Assertion.Level.INFO,
                    "<code class='kw'>meta</code> encoding declarations don't work with XML",
                    "Unless you sometimes serve this page as <code"
                    + " class='kw'>text/html</code>, remove the <code"
                    + " class='kw'>meta</code> tag and ensure you have an XML"
                    + " declaration with encoding information.",
                    Arrays.asList(parsedDocument.getCharsetMetaContext())));
        }
    }

    // rep_charset_multiple_meta (ERROR)
    // "CHARSET REPORT: Multiple encoding declarations using the meta tag"
    private void addAssertionRepCharsetMultipleMeta() {
        if (parsedDocument.hasMultipleMetas()) {
            assertions.add(new Assertion(
                    "rep_charset_multiple_meta",
                    Assertion.Level.ERROR,
                    "Multiple encoding declarations using the <code"
                    + " class='kw'>meta</code> tag",
                    "Edit the markup to remove all but one <code"
                    + " class='kw'>meta</code> element.",
                    new ArrayList<String>()));
        }
    }

    // rep_charset_no_effective_charset (WARNING)
    // "CHARSET REPORT: No effective character encoding information"
    private void addAssertionRepCharsetNoEffectiveCharset() {
        if (parsedDocument.getCharsetXmlDeclaration() != null
                && parsedDocument.getCharsetHttp() == null
                && parsedDocument.getByteOrderMark() == null
                && parsedDocument.getCharsetMeta() == null) {
            if (parsedDocument.isHtml()
                    || parsedDocument.isHtml5()
                    || (parsedDocument.isXhtml10()
                    && !parsedDocument.isServedAsXml())) {
                assertions.add(new Assertion(
                        "rep_charset_no_effective_charset",
                        Assertion.Level.WARNING,
                        "No effective character encoding information",
                        "Add a <code class='kw'>meta</code> element to indicate the"
                        + " character encoding of the page. You could also"
                        + " declare the encoding in the HTTP header, but it is"
                        + " recommended that you always use a <code"
                        + " class='kw'>meta</code> element too.",
                        new ArrayList<String>()));
            }
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
                && parsedDocument.getCharsetMeta() == null) {

            assertions.add(new Assertion(
                    "rep_charset_no_visible_charset",
                    Assertion.Level.WARNING,
                    "No visible in-document encoding declared",
                    "Add a <code class='kw'>meta</code> tag or XML declaration,"
                    + " as appropriate, to your page to indicate the character"
                    + " encoding used.",
                    new ArrayList<String>()));
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
        if (parsedDocument.isHtml5()
                && parsedDocument.getCharsetMeta() != null
                // TODO: Review and deal with multiple meta tags. ~~~ Joe.
                && parsedDocument.getCharsetMetaContext()
                .contains("http-equiv")) {
            assertions.add(new Assertion(
                    "rep_charset_pragma",
                    Assertion.Level.INFO,
                    "<code class='kw'>meta</code> character encoding"
                    + " declaration uses <code class='kw'>http-equiv</code>",
                    "Replace the <code class='kw'>http-equiv</code> and <code"
                    + " class='kw'>content</code> attributes in your <code"
                    + " class='kw'>meta</code> tag with a <code"
                    + " class='kw'>charset</code> attribute.",
                    Arrays.asList(parsedDocument.getCharsetMetaContext())));
        }
    }

    // rep_charset_utf16_meta (ERROR)
    // "CHARSET REPORT: Meta character encoding declaration used in UTF-16 page"
    private void addAssertionRepCharsetUtf16Meta() {
        boolean charsetMetaUtf16;
        if (parsedDocument.getCharsetMeta() != null) {
            charsetMetaUtf16 = parsedDocument.getCharsetMeta().toLowerCase()
                    .contains("utf-16");
        } else {
            charsetMetaUtf16 = false;
        }
        if (charsetMetaUtf16) {
            if (parsedDocument.isUtf16() && parsedDocument.isHtml5()) {
                assertions.add(new Assertion(
                        "rep_charset_utf16_meta",
                        Assertion.Level.ERROR,
                        "Meta character encoding declaration used in UTF-16"
                        + " page",
                        "Remove the <code class='kw'>meta</code> encoding"
                        + " declaration.",
                        Arrays.asList(parsedDocument.getCharsetMetaContext())));
            }
        }
    }

    // rep_charset_utf16lebe (ERROR)
    private void addAssertionRepCharsetUtf16lebe() {
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
                        + " class='kw'>meta</code> element instead to declare"
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
    private void addAssertionRepLangConflict() {
    }

    // rep_lang_content_lang_meta (ERROR)
    // rep_lang_content_lang_meta (WARNING)
    private void addAssertionRepLangContentLangMeta() {
    }

    // rep_lang_html_no_effective_lang (WARNING)
    private void addAssertionRepLangHtmlNoEffectiveLang() {
    }

    // rep_lang_malformed_attr (ERROR)
    private void addAssertionRepLangMalformedAttr() {
    }

    // rep_lang_missing_html_attr (ERROR)
    // rep_lang_missing_html_attr (WARNING)
    private void addAssertionRepLangMissingHtmlAttr() {
    }

    // rep_lang_missing_xml_attr (ERROR)
    // rep_lang_missing_xml_attr (WARNING)
    private void addAssertionRepLangMissingXmlAttr() {
    }

    // rep_lang_no_lang_attr (INFO)
    // rep_lang_no_lang_attr (WARNING)
    private void addAssertionRepLangNoLangAttr() {
    }

    // rep_lang_xml_attr_in_html (ERROR)
    private void addAssertionRepLangXmlAttrInHtml() {
    }

    // rep_latin_non_nfc (WARNING)
    private void addAssertionRepLatinNonNfc() {
    }

    // rep_markup_bdo_no_dir (INFO)
    private void addAssertionRepMarkupBdoNoDir() {
    }

    // rep_markup_dir_incorrect (ERROR)
    // rep_markup_dir_incorrect (INFO)
    private void addAssertionRepMarkupDirIncorrect() {
    }

    // rep_markup_tags_no_class (INFO)
    private void addAssertionRepMarkupTagsNoClass() {
    }
}
