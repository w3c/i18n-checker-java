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
import com.ning.http.client.Response;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.w3.assertor.model.Assertion;
import org.w3.assertor.Assertor;

/**
 *
 * @author Joseph J Short
 */
public class I18nChecker implements Assertor {

    ParsedDocument parsedDocument;
    List<Assertion> assertions;

    @Override
    public Iterable<Assertion> assertUri(
            URI uri, AsyncHttpClient asyncHttpClient, String[] options) {

        /* 
         * TODO: Decide whether to pass these as parameters to the addAssertion
         * methods or to ensure thread-safety with syncronisation or to leave
         * 'as-is'.
         */
        parsedDocument = get(uri, asyncHttpClient);
        assertions = new ArrayList<>();

        addAssertionDtdMimetype();
        addAssertionCharsetHttp();
        
        return assertions;
    }

    private void addAssertionDtdMimetype() {
        if (parsedDocument.isHtml5()) {
            assertions.add(new Assertion(
                    "message_xhtml5_partial_support",
                    Assertion.Level.MESSAGE,
                    null,
                    // TODO: Update description from previous project.
                    "This is an xhtml5 document. xhtml5 specifics are not yet"
                    + " integrated in the checker so you may have inacurate"
                    + " results.",
                    null));
        }
        assertions.add(new Assertion(
                "dtd",
                Assertion.Level.INFO,
                null,
                parsedDocument.getDoctypeDescription(),
                Arrays.asList(parsedDocument.getDoctypeDeclaration())));
        assertions.add(new Assertion(
                "mimetype",
                Assertion.Level.INFO,
                null,
                parsedDocument.getResponse().getContentType(),
                null));
    }

    public void addAssertionCharsetHttp() {
        String contentType = parsedDocument.getResponse().getContentType();
        String context = "Content-Type: " + contentType;
        String value = Utils.getCharsetFromContentType(contentType);
        String description =
                value == null ? context == null
                ? "val_none_found" : "charset_none_found" : null;
        assertions.add(new Assertion(
                "charset_http",
                Assertion.Level.INFO,
                null,
                description,
                Arrays.asList(context)));

    }

    private static ParsedDocument get(
            URI uri, AsyncHttpClient asyncHttpClient) {
        Response response = null;
        try {
            response =
                    asyncHttpClient.prepareGet(uri.toString()).execute().get();
        } catch (IOException | InterruptedException | ExecutionException ex) {
            throw new RuntimeException(
                    "Exception thrown when retrieving document.", ex);
        }
        return new ParsedDocument(response);
    }
}
