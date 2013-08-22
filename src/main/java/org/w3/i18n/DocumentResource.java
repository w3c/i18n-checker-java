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
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
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

    public DocumentResource(
            URL url, InputStream body, Map<String, List<String>> headers) {
        this.url = url;
        this.body = body;
        this.headers = getCaseInsensitiveHeaders(headers);
    }

    public URL getUrl() {
        return url;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public String getHeader(String key) {
        String header = null;
        if (headers.containsKey(key.toLowerCase())) {
            StringBuilder sb = new StringBuilder();
            for (String string : headers.get(key.toLowerCase())) {
                sb.append(string);
            }
            header = sb.toString();
        }
        return header;
    }

    public InputStream getBody() {
        return body;
    }

    public static DocumentResource getRemote(URL url) throws IOException {
        DocumentResource documentResource;
        try (AsyncHttpClient asyncHttpClient = new AsyncHttpClient()) {
            documentResource = getRemote(asyncHttpClient, url);
        }
        return documentResource;
    }

    public static Map<URL, DocumentResource> getRemote(Set<URL> urls)
            throws IOException {
        if (urls == null) {
            throw new NullPointerException("urls: " + urls + ".");
        }
        // Create a DocumentResource for each URL.
        Map<URL, DocumentResource> results = new HashMap<>();
        try (AsyncHttpClient asyncHttpClient = new AsyncHttpClient()) {
            for (URL url : urls) {
                results.put(url, getRemote(asyncHttpClient, url));
            }
        }
        return results;
    }

    private static DocumentResource getRemote(
            AsyncHttpClient asyncHttpClient, URL url) throws IOException {
        if (asyncHttpClient == null || url == null) {
            throw new NullPointerException("asyncHttpClient: " + asyncHttpClient
                    + ", url: " + url + ".");
        }
        if (asyncHttpClient.isClosed()) {
            throw new IllegalArgumentException("Passed an AsyncHttpClient that"
                    + " is closed. asyncHttpClient: " + asyncHttpClient);
        }
        // Retrieve the remote document with the HTTP client.
        Response response;
        try {
            response = asyncHttpClient.executeRequest(
                    asyncHttpClient.prepareGet(url.toExternalForm()).build())
                    .get();
        } catch (InterruptedException | ExecutionException ex) {
            throw new RuntimeException(
                    "Problem retrieving remote document. asyncHttpClient: "
                    + asyncHttpClient + ", url: " + url + ".", ex);
        }
        // Create a DocumentResource from the response.
        return new DocumentResource(
                url, response.getResponseBodyAsStream(),
                getCaseInsensitiveHeaders(response.getHeaders()));
    }

    private static Map<String, List<String>> getCaseInsensitiveHeaders(
            Map<String, List<String>> headers) {
        Map<String, List<String>> caseInsensitiveHeaders =
                new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        caseInsensitiveHeaders.putAll(headers);
        return caseInsensitiveHeaders;
    }
}
