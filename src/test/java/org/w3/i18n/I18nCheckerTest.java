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

import org.junit.Test;

/**
 *
 * @author Joseph J Short
 */
public class I18nCheckerTest {

    public I18nCheckerTest() {
    }

    @Test(expected = NullPointerException.class)
    public void testCheck_URL() throws Exception {
        System.out.println("Testing (invalid parameters): check(URL)");
        I18nChecker.check(null);
    }

    @Test(expected = NullPointerException.class)
    public void testCheck_3args() {
        System.out.println("Testing (invalid parameters): check(URL,"
                + " InputStream, Map<String, List<String>>)");
        I18nChecker.check(null, null, null);
    }
}