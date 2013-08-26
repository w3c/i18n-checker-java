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

import java.net.URL;
import java.util.List;

/**
 *
 * @author Joseph J Short
 */
public class I18nTest implements Comparable<I18nTest> {

    private final String name;
    private final String description;
    private final String id;
    private final URL url;
    private final String format;
    private final String serveAs;
    private final List<Assertion> expectedAssertions;

    public I18nTest(
            String name,
            String description,
            String id,
            URL url,
            String format,
            String serveAs,
            List<Assertion> expectedAssertions) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.url = url;
        this.format = format;
        this.serveAs = serveAs;
        this.expectedAssertions = expectedAssertions;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getId() {
        return id;
    }

    public URL getUrl() {
        return url;
    }

    public String getFormat() {
        return format;
    }

    public String getServeAs() {
        return serveAs;
    }

    public List<Assertion> getExpectedAssertions() {
        return expectedAssertions;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ":[" + name + ", "
                + description + ", " + id + ", " + url + ", " + format + ", "
                + serveAs + ", " + expectedAssertions + "]";
    }

    @Override
    public int compareTo(I18nTest other) {
        return name.compareTo(other.name) < 0 ? -1
                : name.compareTo(other.name) > 0 ? 1
                : format.compareTo(other.format) < 0 ? -1
                : format.compareTo(other.format) > 0 ? 1
                : serveAs.compareTo(other.serveAs) < 0 ? -1
                : serveAs.compareTo(other.serveAs) > 0 ? 1
                : 0;
    }
}
