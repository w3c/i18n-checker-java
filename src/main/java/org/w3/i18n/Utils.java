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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Joseph J Short
 */
class Utils {

    private Utils() {
    }

    public static String getCharsetFromContentType(String contentType) {
        if (contentType == null) {
            throw new NullPointerException();
        }
        String charset;
        Matcher charsetMatcher =
                Pattern.compile("charset=[^;]*").matcher(contentType);
        if (charsetMatcher.find()) {
            charset = charsetMatcher.group().substring(8).trim();
        } else {
            charset = null;
        }
        return charset;
    }

    public static String getCharsetFromXmlDeclaration(String xmlDeclaration) {
        if (xmlDeclaration == null) {
            throw new NullPointerException();
        }
        String charset;
        Matcher charsetMatcher =
                // TODO Single quotes here?
                Pattern.compile("encoding=\"[^\"]*").matcher(xmlDeclaration);
        if (charsetMatcher.find()) {
            charset = charsetMatcher.group().substring(10).trim();
        } else {
            charset = null;
        }
        return charset;
    }
}
