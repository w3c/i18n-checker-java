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

    @Override
    public String toString() {
        return "[" + id + "; " + level + "; " + htmlTitle + "; "
                + htmlDescription + "; " + contexts + "]";
    }

    @Override
    public boolean equals(Object obj) {
        boolean equals;
        if (obj == this) {
            equals = true;
        } else if (!(obj instanceof Assertion)) {
            equals = false;
        } else {
            Assertion a = (Assertion) obj;
            /* TODO This equals method should use all the properties of
             * Assertion. It is currently simplified for debugging purposes.
             * ~~~ Joe. */
            equals = a.id == null ? a.id == null : a.id.equals(this.id);
//            equals = a.id.equals(this.id)
//                    ? a.level.equals(this.level)
//                    ? a.htmlTitle.equals(this.htmlTitle)
//                    ? a.htmlDescription.equals(this.htmlDescription)
//                    ? a.contexts.equals(this.contexts)
//                    : false : false : false : false;
        }
        return equals;
    }

    @Override
    public int hashCode() {
        int hashCode = 11;
        hashCode = hashCode * 31 + (id == null ? 0 : id.hashCode());
        hashCode = hashCode * 31 + (level.hashCode());
        hashCode = hashCode * 31
                + (htmlTitle == null ? 0 : htmlTitle.hashCode());
        hashCode = hashCode * 31
                + (htmlDescription == null ? 0 : htmlDescription.hashCode());
        hashCode = hashCode * 31 + (contexts == null ? 0 : contexts.hashCode());
        return hashCode;
    }
}