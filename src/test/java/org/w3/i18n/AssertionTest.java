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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Joseph J Short
 */
public class AssertionTest {

    private static Assertion a;
    private static Assertion b;
    private static Assertion c;

    public AssertionTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        a = new Assertion(
                "test", Assertion.Level.INFO, "test", "",
                Arrays.asList("test", "test"));
        b = new Assertion(
                "test", Assertion.Level.INFO, "test", "",
                Arrays.asList("test", "test"));
        c = new Assertion(
                "test", Assertion.Level.INFO, "test", "",
                Arrays.asList("test", "test"));
    }

    @Test
    public void testEquals() {
        assertEquals(a, b);
        assertEquals(b, a);
        assertEquals(c, a);
        // TODO: update with the equals() method in Assertion.
        // assertNotEquals(a, d);
    }

    @Test
    public void testHashCode() {
        assertEquals(a.hashCode(), b.hashCode());
        assertEquals(b.hashCode(), a.hashCode());
        assertEquals(c.hashCode(), a.hashCode());
    }

    @Test
    public void testSerialization() {
        try {
            // Create the test Assertion.
            Assertion assertion = new Assertion(
                    "test_ser", Assertion.Level.MESSAGE,
                    "Test serialization", "This is only a test.",
                    Arrays.asList("A", "B", "C"));
            // Create the output stream.
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            // Write the assertion to the stream.
            oos.writeObject(assertion);
            // Create the input stream.
            ByteArrayInputStream bais =
                    new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            // Read back the serialized Assertion.
            Assertion serializedAssertion = (Assertion) ois.readObject();
            // Compare it with the original.
            assertEquals("The serialised assertion is not the same as it was"
                    + " before serialization. Assertion: " + assertion
                    + ", serializedAssertion: " + serializedAssertion,
                    assertion, serializedAssertion);
        } catch (IOException | ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }

    }
}