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
import com.ning.http.client.Request;
import com.ning.http.client.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * A {@code DocumentResource} is a aggregation of the materials needed to make a
 * {@link ParsedDocument}.
 *
 * @author Joseph J Short
 */
class DocumentResource {

    private final URL url;
    private final Map<String, List<String>> headers;
    private final InputStream body;

    public DocumentResource(URL url) throws IOException {
        this.url = url;
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        Response response = null;
        try {
            Request request =
                    asyncHttpClient.prepareGet(url.toExternalForm()).build();
            response = asyncHttpClient.executeRequest(request).get();
            this.body = response.getResponseBodyAsStream();
        } catch (InterruptedException | ExecutionException ex) {
            throw new RuntimeException(
                    "Exception thrown when retrieving document.", ex);
        } finally {
            asyncHttpClient.close();
        }
        this.headers = response.getHeaders();
    }

    public DocumentResource(
            URL url, InputStream body, Map<String, List<String>> headers) {
        this.url = url;
        this.body = body;
        this.headers = headers;
    }

    public URL getUrl() {
        return url;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public String getHeader(String key) {
        String header = null;
        if (headers.containsKey(key)) {
            StringBuilder sb = new StringBuilder();
            for (String string : headers.get(key)) {
                sb.append(sb);
            }
            header = sb.toString();
        }
        return header;
    }

    public InputStream getBody() {
        return body;
    }
}
