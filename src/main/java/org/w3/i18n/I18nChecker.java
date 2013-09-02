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
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * This class provides two methods for performing internationalisation (i18n)
 * checks on web documents.
 *
 * @author Joseph J Short
 */
public final class I18nChecker {

    // Noninstantiable class.
    private I18nChecker() {
    }

    /**
     * Runs a check on a remote document at the given {@code URL} and returns
     * the results as a list of {@link Assertion}. The remote document is
     * retrieved using an HTTP request.
     *
     * @param url the URL of the remote document to check.
     * @return a list of {@link Assertion} which contain information about the
     * internationalisation of the document.
     * @throws IOException if the remote document cannot be retrieved for
     * checking.
     */
    public static List<Assertion> check(URL url) throws IOException {
        return new Check(new ParsedDocument(DocumentResource.getRemote(url)))
                .getAssertions();
    }

    /**
     * Runs a check on the given document body and HTTP response headers as if
     * they were a remote document, returns the results as a list of
     * {@link Assertion}. This method sends no HTTP requests.
     *
     * @param url the URL of the document.
     * @param body the body of the document (should be text and HTML).
     * @param headers the HTTP response headers obtained by retrieving the
     * document from an HTTP server.
     * @return a list of {@link Assertion} which contain information about the
     * internationalisation of the document.
     */
    public static List<Assertion> check(
            URL url, InputStream body, Map<String, List<String>> headers) {
        return new Check(
                new ParsedDocument(new DocumentResource(url, body, headers)))
                .getAssertions();
    }
}
