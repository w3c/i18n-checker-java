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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3.i18n.Assertion.Level;

/**
 * Provides titles and descriptions for {@link Assertion}s when given a known id
 * and {@code Level}. Backed by a properties file.
 *
 * @author Joseph J Short
 */
class AssertionProvider {

    private static final Map<Key, Value> definitions;
    private static final Logger logger =
            LoggerFactory.getLogger(AssertionProvider.class);

    // Noninstantiable class.
    private AssertionProvider() {
    }

    static {
        // Load and parse assertion.properties file.
        // TODO: move this constant to the configuration.
        final String DEFINITIONS_FILE = "assertions-EN.properties";
        logger.info("Reading assertion definitions file: " + DEFINITIONS_FILE);
        Configuration configuration;
        try {
            configuration = new PropertiesConfiguration(DEFINITIONS_FILE);
        } catch (ConfigurationException ex) {
            throw new RuntimeException(
                    "assertion properties file: " + DEFINITIONS_FILE, ex);
        }

        // Read the definitions.
        definitions = new TreeMap<>();
        interpretAssertionDefinitions(configuration);
        logger.info("Found " + definitions.size() + " definition(s).");
    }

    private static void interpretAssertionDefinitions(
            Configuration configuration) {
        // Find all prefixes.
        Set<String> prefixes = new TreeSet<>();
        Iterator<String> keyIterator = configuration.getKeys();
        while (keyIterator.hasNext()) {
            String key = keyIterator.next();
            String[] keySplit = key.split("\\.");
            boolean invalid = false;
            if (keySplit.length != 3) {
                invalid = true;
            } else {
                if (keySplit[0].trim().isEmpty()
                        || keySplit[1].trim().isEmpty()
                        || keySplit[2].trim().isEmpty()
                        || (!keySplit[2].trim().equalsIgnoreCase("title")
                        && !keySplit[2].trim()
                        .equalsIgnoreCase("description"))) {
                    invalid = true;
                } else {
                    try {
                        Assertion.Level.valueOf(
                                keySplit[1].trim().toUpperCase());
                    } catch (IllegalArgumentException e) {
                        invalid = true;
                    }
                }
            }
            if (invalid) {
                throw new RuntimeException("Invalid key in assertion"
                        + " configuration. Keys should be of the form"
                        + " \"<assertion_id>.<assertion_level>."
                        + "{title|description}\". Key: " + key
                        + ", configuration: " + configuration);

            }
            prefixes.add(keySplit[0] + "." + keySplit[1]);
        }
        for (String prefix : prefixes) {
            interpretAssertionDefinition(prefix, configuration);
        }
    }

    private static void interpretAssertionDefinition(
            String prefix, Configuration configuration) {
        // Retieve the required properties for the prefix.
        String titleProperty = configuration.containsKey(prefix + ".title")
                ? configuration.getString(prefix + ".title") : null;
        String descriptionProperty = configuration.containsKey(
                prefix + ".description")
                ? configuration.getString(prefix + ".description") : null;
        if (titleProperty == null || descriptionProperty == null) {
            throw new RuntimeException("Invalid assertion definition in"
                    + " configuration. Each definition should have a 'title'"
                    + " and 'description' property. Prefix: " + prefix
                    + ", title property: \"" + titleProperty
                    + "\", description property: \"" + descriptionProperty
                    + "\", configuration: " + configuration);
        }

        // Prepare definition details.
        String[] prefixSplit = prefix.split("\\.");
        String id = prefixSplit[0].trim();
        Level level = Level.valueOf(prefixSplit[1].trim());
        String title = titleProperty.trim();
        String description = descriptionProperty.trim();

        // Check for duplicate definition key.
        Key key = new Key(id, level);
        if (definitions.containsKey(key)) {
            throw new RuntimeException("Duplicate assertion definition in"
                    + " configuration. Prefix: " + prefix
                    + ", existsing title property: \""
                    + definitions.get(key).htmlTitle
                    + ", current title property: \"" + title
                    + "\", configuration: " + configuration);
        }

        definitions.put(key, new Value(title, description));
    }

    /**
     * Finds an assertion definition that matches the id and level and returns a
     * new assertion that has the given contexts.
     *
     * @param id the assertion id to search for.
     * @param level the {@link Assertion} Level to search for.
     * @param contexts the list of contexts to give the new Assertion.
     * @return a new assertion that has the given contexts.
     */
    public static Assertion getForWith(
            String id, Assertion.Level level, List<String> contexts) {
        if (id == null || level == null || contexts == null) {
            throw new NullPointerException("id: " + id + ", level: " + level
                    + ", contexts: " + contexts);
        }
        Value definition = definitions.get(new Key(id, level));
        if (definition == null) {
            throw new IllegalArgumentException("This assertion provider has no"
                    + " definition for id: " + id + ", level :" + level);
        }
        return new Assertion(
                id,
                level,
                definition.htmlTitle,
                definition.htmlDescription,
                contexts);
    }

    private static class Key implements Comparable<Key> {

        private final String id;
        private final Assertion.Level level;

        public Key(String id, Assertion.Level level) {
            if (id == null || level == null) {
                throw new NullPointerException(
                        "id: " + id + ", level: " + level);
            }
            this.id = id;
            this.level = level;
        }

        @Override
        public int compareTo(Key k) {
            return id.compareTo(k.id) < 0 ? -1
                    : id.compareTo(k.id) > 0 ? 1
                    : level.compareTo(k.level) < 0 ? -1
                    : level.compareTo(k.level) > 0 ? 1
                    : 0;
        }
    }

    private static class Value {

        final String htmlTitle;
        final String htmlDescription;

        public Value(String htmlTitle, String htmlDescription) {
            if (htmlTitle == null || htmlDescription == null) {
                throw new NullPointerException("htmlTitle: " + htmlTitle
                        + ", htmlDescription: " + htmlDescription);
            }
            this.htmlTitle = htmlTitle;
            this.htmlDescription = htmlDescription;
        }
    }
}
