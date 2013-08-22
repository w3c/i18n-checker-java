/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.w3.i18n;

import java.util.Arrays;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
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
    private static Assertion d;

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
        d = new Assertion(
                "test", Assertion.Level.INFO, "test", "",
                Arrays.asList("1"));
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
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
}