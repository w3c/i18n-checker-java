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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;

/**
 *
 * @author Joseph J Short
 */
class Utils {

    private Utils() {
    }

    /**
     * Returns a list of subsequences of a {@code CharSequence} that match the
     * given {@code Pattern}, or an empty list if there are no matches.
     *
     * @param pattern The pattern to use for searching.
     * @param input The CharSequence to search.
     * @return a list of subsequences of a {@code CharSequence} that match the
     * given {@code Pattern}, or an empty list if there are no matches.
     * @throws NullPointerException for a null parameter.
     */
    public static List<String> getMatchingGroups(
            Pattern pattern, CharSequence input) {
        if (pattern == null) {
            throw new NullPointerException("Parameter <Pattern pattern>.");
        }
        if (input == null) {
            throw new NullPointerException("Parameter <CharSequence input>.");
        }
        List<String> matchingGroups = new ArrayList<>();
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            matchingGroups.add(matcher.group());
        }
        return matchingGroups;
    }

    public static ByteOrderMark findByteOrderMark(byte[] bytes) {
        if (bytes == null) {
            throw new NullPointerException("bytes: " + bytes);
        }
        ByteOrderMark byteOrderMark;
        try {
            byteOrderMark = new BOMInputStream(
                    new ByteArrayInputStream(bytes),
                    ByteOrderMark.UTF_8,
                    ByteOrderMark.UTF_16BE,
                    ByteOrderMark.UTF_16LE,
                    ByteOrderMark.UTF_32BE,
                    ByteOrderMark.UTF_32LE).getBOM();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return byteOrderMark;
    }
}
