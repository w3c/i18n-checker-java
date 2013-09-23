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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * An {@code Assertion} has an ID, level, HTML title, HTML description, and a
 * list of contexts; together these items say something thing about a
 * {@link DocumentResource} that was checked by a {@link Check} object. The
 * results of an i18n check are given as a List of {@code Assertion}; one
 * {@code Assertion} is the result of one part of a check. {@code Assertions}
 * can be formatted and displayed to the user of your code, to convey
 * information about the i18n of the document they had checked.
 *
 * {@code Assertions} are immutable and serialisable. None of the details
 * exposed by an {@code Assertion} are ever {@code null}.
 *
 * @author Joseph J Short
 */
public class Assertion implements Comparable<Assertion>, Serializable {

    static final long serialVersionUID = 90437656459515251L;
    private final String id;
    private final Level level;
    private final String htmlTitle;
    private final String htmlDescription;
    private final List<String> contexts;

    /**
     * Creates a new {@code Assertion} object with all the details given as the
     * arguments.
     *
     * @param id a recognisable name for an {@code Assertion} that can be used
     * to distinguish between {@code Assertions} in client code.
     * @param level describes the importance of an {@code Assertion} (see
     * {@link Level}).
     * @param htmlTitle A short title that describes the meaning and content of
     * the {@code Assertion}.
     * @param htmlDescription A paragraph that describes the meaning and content
     * of the {@code Assertion} in detail.
     * @param contexts A list of Strings, often verbatim extracts from the
     * document, which are a reference back to the document.
     * @throws NullPointerException if any arguments are null.
     */
    public Assertion(
            String id,
            Level level,
            String htmlTitle,
            String htmlDescription,
            List<String> contexts) {
        if (id == null || level == null || htmlTitle == null
                || htmlDescription == null || contexts == null) {
            throw new NullPointerException("id: " + id + ", level: " + level
                    + ", htmlTitle: " + htmlTitle + ", htmlDescription: "
                    + htmlDescription + ", contexts: " + contexts + ".");
        }
        if (id.isEmpty()) {
            throw new IllegalArgumentException(
                    "Given id is an empty String.");
        }
        this.id = id;
        this.level = level;
        this.htmlTitle = htmlTitle;
        this.htmlDescription = htmlDescription;
        this.contexts = contexts;
    }

    /**
     * The id is a recognisable name for an {@code Assertion} that can be used
     * to distinguish between {@code Assertions} in client code. Ids are not
     * necessarily useful to end users unless they're given context.
     *
     * @return the id of the {@code Assertion}.
     */
    public String getId() {
        return id;
    }

    /**
     * The {@link Level} describes the importance of an {@code Assertion} (see
     * {@link Level}).
     *
     * @return the {@link Level} of the {@code Assertion}.
     */
    public Level getLevel() {
        return level;
    }

    /**
     * A short title that describes the meaning and content of the
     * {@code Assertion}. May contain HTML markup.
     *
     * @return the {@code Assertion's} title.
     */
    public String getHtmlTitle() {
        return htmlTitle;
    }

    /**
     * A paragraph that describes the meaning and content of the
     * {@code Assertion} in detail. In a case where the {@code Assertion}
     * identifies an i18n problem with a document, the description may contain
     * instructions on how to remedy the problem. May contain HTML markup.
     *
     * @return the {@code Assertion's} description.
     */
    public String getHtmlDescription() {
        return htmlDescription;
    }

    /**
     * A list of Strings, often verbatim extracts from the document, which are a
     * reference back to the document. Contexts often justify or substantiate an
     * {@code Assertion}. In some case there may be no contexts, in which case
     * this method returns an empty list. Contexts may need to be 'cleaned'
     * before usage as they can contain original white-spaces and markup from
     * the document.
     *
     * @return the contexts of an {@code Assertion}.
     */
    public List<String> getContexts() {
        synchronized (contexts) {
            return new ArrayList<>(contexts);
        }
    }

    /**
     * The {@code Level} describes the importance of an {@code Assertion}.
     */
    public enum Level {

        /**
         * {@code INFO} level {@code Assertions} provide useful observations
         * about a document which are relevant to i18n. The author of the
         * document may find these {@code Assertions} useful, but does not
         * necessarily need to act upon them.
         */
        INFO,
        /**
         * {@code WARNING} level {@code Assertions} describe possible i18n
         * problems with the document. These problems are not contraventions of
         * rules or specifications, but may nevertheless cause problems if the
         * document is internationalised.
         */
        WARNING,
        /**
         * {@code ERROR} level {@code Assertions} describe serious i18n problems
         * with the document. These problems are often contraventions of
         * official specifications or guidelines, and are very likely to cause
         * problems when internationalising the document.
         */
        ERROR,
        /**
         * {@code MESSAGE} level {@code Assertions} only contain
         * meta-information about the operation of the checker. They are
         * unrelated to the i18n of the document.
         */
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
            equals = a.id.equals(this.id)
                    ? a.level.equals(this.level)
                    ? a.htmlTitle.equals(this.htmlTitle)
                    ? a.htmlDescription.equals(this.htmlDescription)
                    ? a.contexts.equals(this.contexts)
                    : false : false : false : false;
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

    @Override
    public int compareTo(Assertion a) {
        int result = id.compareTo(a.id) < 0 ? -1
                : id.compareTo(a.id) > 0 ? 1
                : level.compareTo(a.level) < 0 ? -1
                : level.compareTo(a.level) > 0 ? 1
                : htmlTitle.compareTo(a.htmlTitle) < 0 ? -1
                : htmlTitle.compareTo(a.htmlTitle) > 0 ? 1
                : htmlDescription.compareTo(a.htmlDescription) < 0 ? -1
                : htmlDescription.compareTo(a.htmlDescription) > 0 ? 1
                : 0;
        // TODO compare contexts?
        if (result == 0) {
            result = contexts.size() < a.contexts.size() ? -1
                    : contexts.size() > a.contexts.size() ? 1
                    : 0;
        }
        return result;
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
    }

    private void readObject(ObjectInputStream ois)
            throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        if (id == null || level == null || htmlTitle == null
                || htmlDescription == null || contexts == null) {
            throw new NullPointerException("id: " + id + ", level: " + level
                    + ", htmlTitle: " + htmlTitle + ", htmlDescription: "
                    + htmlDescription + ", contexts: " + contexts
                    + ", ObjectInputStream: " + ois + ".");
        }
        if (id.isEmpty()) {
            throw new IllegalArgumentException(
                    "Given id is an empty String.");
        }
    }
}