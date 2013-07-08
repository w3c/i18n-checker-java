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
package org.w3.assertor;

import org.w3.assertor.model.Assertion;
import com.ning.http.client.AsyncHttpClient;
import java.net.URI;

/**
 *
 * @author Joseph J Short
 */
public interface Assertor {

    /**
     *
     * @param uri
     * @param asyncHttpClient
     * @param options
     * @return
     */
    public Iterable<Assertion> assertUri(
            URI uri, AsyncHttpClient asyncHttpClient, String[] options);
}
