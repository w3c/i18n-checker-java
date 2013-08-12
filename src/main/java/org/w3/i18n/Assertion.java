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

import java.util.List;

/**
 *
 * @author Joseph J Short
 */
public class Assertion {

    // TODO: Model assertion 'groups'.
    private final String id;
    private final Level level;
    private final String htmlTitle;
    private final String htmlDescription;
    private final List<String> contexts;

    public Assertion(
            String id,
            Level level,
            String htmlTitle,
            String htmlDescription,
            List<String> contexts) {
        this.id = id;
        this.level = level;
        this.htmlTitle = htmlTitle;
        this.htmlDescription = htmlDescription;
        this.contexts = contexts;
    }

    public String getId() {
        return id;
    }

    public Level getLevel() {
        return level;
    }

    public String getHtmlTitle() {
        return htmlTitle;
    }

    public String getHtmlDescription() {
        return htmlDescription;
    }

    public List<String> getContexts() {
        return contexts;
    }

    public enum Level {

        INFO,
        WARNING,
        ERROR,
        MESSAGE;
    }
}