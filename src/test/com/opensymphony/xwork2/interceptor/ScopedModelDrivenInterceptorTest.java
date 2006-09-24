/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.xwork2.interceptor;

import java.util.HashMap;

import junit.framework.TestCase;

import com.opensymphony.xwork2.ObjectFactory;

public class ScopedModelDrivenInterceptorTest extends TestCase {

    protected ScopedModelDrivenInterceptor inter = null;
    
    public ScopedModelDrivenInterceptorTest(String name) throws Exception {
        super(name);
    }


    public static void main(String args[]) {
        junit.textui.TestRunner.run(ScopedModelDrivenInterceptorTest.class);
    }

    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() throws Exception {
    
        inter = new ScopedModelDrivenInterceptor();
    }

    public void testResolveModel() throws Exception {
        ObjectFactory factory = ObjectFactory.getObjectFactory();
        Object obj = inter.resolveModel(factory, null, "java.lang.String", "request", null);
        assertNotNull(obj);
        assertTrue(obj instanceof String);

        HashMap session = new HashMap();
        obj = inter.resolveModel(factory, session, "java.lang.String", "session", "foo");
        assertNotNull(obj);
        assertTrue(obj instanceof String);
        assertTrue(obj == session.get("foo"));

        obj = inter.resolveModel(factory, session, "java.lang.String", "session", "foo");
        assertNotNull(obj);
        assertTrue(obj instanceof String);
        assertTrue(obj == session.get("foo"));
    }
}

