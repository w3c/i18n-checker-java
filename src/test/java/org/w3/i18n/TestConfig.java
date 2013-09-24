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

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple application configuration class for the i18n-checker test classes.
 *
 * @author Joseph J Short
 */
final class TestConfig {

    // Noninstantiable class.
    private TestConfig() {
    }
    private static final Logger logger =
            LoggerFactory.getLogger(TestConfig.class);
    // Location of the configuration file.
    private static final String CONFIGURATION_FILE =
            "i18n-checker_tests.properties";
    // Configuration items: Final members that are visible to the package.
    static final String TEST_RUNNER_URL;

    static {
        // Load and parse configuration file.
        logger.info("Reading configuration file: " + CONFIGURATION_FILE);
        Configuration configuration;
        try {
            configuration = new PropertiesConfiguration(CONFIGURATION_FILE);
        } catch (ConfigurationException ex) {
            throw new RuntimeException(
                    "configuration file: " + CONFIGURATION_FILE, ex);
        }

        // Set configuration items.
        TEST_RUNNER_URL = configuration.containsKey("test_runner_url")
                ? configuration.getString("test_runner_url")
                : "a";
        // ...
    }
}
