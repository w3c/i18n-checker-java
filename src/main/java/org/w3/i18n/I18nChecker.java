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
import java.util.concurrent.ExecutionException;
import org.w3.assertor.model.Assertion;
import org.w3.assertor.Assertor;

/**
 *
 * @author Joseph J Short
 */
public class I18nChecker implements Assertor {

    @Override
    public Iterable<Assertion> assertUri(
            URI uri, AsyncHttpClient asyncHttpClient, String[] options) {
        ParsedDocument parsedDocument = get(uri, asyncHttpClient);

        // TODO: Replace debug output with logger(?) or remove.
        System.out.println("Information: DOCTYPE declaration is "
                + parsedDocument.getDoctypeDeclaration() + ".");
        return null;
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
