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
 * This class provides two methods for performing i18n checks on named
 * resources.
 *
 * @author Joseph J Short
 */
public final class I18nChecker {

    // Noninstantiable class.
    private I18nChecker() {
    }

    public static List<Assertion> check(URL url) throws IOException {
        return new Check(DocumentResource.getRemote(url)).getAssertions();
    }

    public static List<Assertion> check(
            URL url, InputStream body, Map<String, List<String>> headers) {
        return new Check(new DocumentResource(url, body, headers))
                .getAssertions();
    }
}
