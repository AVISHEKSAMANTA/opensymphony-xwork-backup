/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.xwork.util;

import junit.framework.TestCase;

import ognl.Ognl;
import ognl.OgnlException;

import java.util.*;


/**
 *
 *
 * @author $Author$
 * @version $Revision$
 */
public class XWorkConverterTest extends TestCase {
    //~ Instance fields ////////////////////////////////////////////////////////

    Map context = Ognl.createDefaultContext(this);
    XWorkConverter converter;

    //~ Methods ////////////////////////////////////////////////////////////////

    public void testStringArrayToCollection() {
        ArrayList list = new ArrayList();
        list.add("foo");
        list.add("bar");
        list.add("baz");
        assertEquals(list, converter.convertValue(context, null, null, null, new String[] {
                    "foo", "bar", "baz"
                }, Collection.class));
    }

    public void testStringArrayToList() {
        ArrayList list = new ArrayList();
        list.add("foo");
        list.add("bar");
        list.add("baz");
        assertEquals(list, converter.convertValue(context, null, null, null, new String[] {
                    "foo", "bar", "baz"
                }, List.class));
    }

    public void testStringArrayToPrimitiveWrappers() {
        Long[] longs = (Long[]) converter.convertValue(context, null, null, null, new String[] {
                "123", "456"
            }, Long[].class);
        assertNotNull(longs);
        assertTrue(Arrays.equals(new Long[] {new Long(123), new Long(456)}, longs));

        Integer[] ints = (Integer[]) converter.convertValue(context, null, null, null, new String[] {
                "123", "456"
            }, Integer[].class);
        assertNotNull(ints);
        assertTrue(Arrays.equals(new Integer[] {
                    new Integer(123), new Integer(456)
                }, ints));

        Double[] doubles = (Double[]) converter.convertValue(context, null, null, null, new String[] {
                "123", "456"
            }, Double[].class);
        assertNotNull(doubles);
        assertTrue(Arrays.equals(new Double[] {new Double(123), new Double(456)}, doubles));

        Boolean[] booleans = (Boolean[]) converter.convertValue(context, null, null, null, new String[] {
                "true", "false"
            }, Boolean[].class);
        assertNotNull(booleans);
        assertTrue(Arrays.equals(new Boolean[] {Boolean.TRUE, Boolean.FALSE}, booleans));
    }

    public void testStringArrayToPrimitives() throws OgnlException {
        long[] longs = (long[]) converter.convertValue(context, null, null, null, new String[] {
                "123", "456"
            }, long[].class);
        assertNotNull(longs);
        assertTrue(Arrays.equals(new long[] {123, 456}, longs));

        int[] ints = (int[]) converter.convertValue(context, null, null, null, new String[] {
                "123", "456"
            }, int[].class);
        assertNotNull(ints);
        assertTrue(Arrays.equals(new int[] {123, 456}, ints));

        double[] doubles = (double[]) converter.convertValue(context, null, null, null, new String[] {
                "123", "456"
            }, double[].class);
        assertNotNull(doubles);
        assertTrue(Arrays.equals(new double[] {123, 456}, doubles));

        boolean[] booleans = (boolean[]) converter.convertValue(context, null, null, null, new String[] {
                "true", "false"
            }, boolean[].class);
        assertNotNull(booleans);
        assertTrue(Arrays.equals(new boolean[] {true, false}, booleans));
    }

    public void testStringArrayToSet() {
        HashSet list = new HashSet();
        list.add("foo");
        list.add("bar");
        list.add("baz");
        assertEquals(list, converter.convertValue(context, null, null, null, new String[] {
                    "foo", "bar", "bar", "baz"
                }, Set.class));
    }

    public void testStringToPrimitiveWrappers() {
        assertEquals(new Long(123), converter.convertValue(context, null, null, null, "123", Long.class));
        assertEquals(new Integer(123), converter.convertValue(context, null, null, null, "123", Integer.class));
        assertEquals(new Double(123.5), converter.convertValue(context, null, null, null, "123.5", Double.class));
        assertEquals(new Boolean(false), converter.convertValue(context, null, null, null, "false", Boolean.class));
        assertEquals(new Boolean(true), converter.convertValue(context, null, null, null, "true", Boolean.class));
    }

    public void testStringToPrimitives() {
        assertEquals(new Long(123), converter.convertValue(context, null, null, null, "123", long.class));
        assertEquals(new Integer(123), converter.convertValue(context, null, null, null, "123", int.class));
        assertEquals(new Double(123.5), converter.convertValue(context, null, null, null, "123.5", double.class));
        assertEquals(new Boolean(false), converter.convertValue(context, null, null, null, "false", boolean.class));
        assertEquals(new Boolean(true), converter.convertValue(context, null, null, null, "true", boolean.class));
    }

    protected void setUp() throws Exception {
        converter = XWorkConverter.getInstance();
    }

    protected void tearDown() throws Exception {
        XWorkConverter.resetInstance();
    }
}
