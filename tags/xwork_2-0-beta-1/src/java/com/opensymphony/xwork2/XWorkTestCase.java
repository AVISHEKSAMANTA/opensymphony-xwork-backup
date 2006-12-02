/*
 * Copyright (c) 2002-2006 by OpenSymphony
 * All rights reserved.
 */

package com.opensymphony.xwork2;

import com.opensymphony.xwork2.config.ConfigurationManager;
import com.opensymphony.xwork2.util.LocalizedTextUtil;
import com.opensymphony.xwork2.util.OgnlValueStack;
import com.opensymphony.xwork2.util.ValueStack;
import com.opensymphony.xwork2.util.ValueStack;
import com.opensymphony.xwork2.util.XWorkConverter;
import junit.framework.TestCase;

/**
 * Base JUnit TestCase to extend for XWork specific unit tests.
 *
 * @author plightbo
 */
public abstract class XWorkTestCase extends TestCase {

    protected ConfigurationManager configurationManager = new ConfigurationManager();
    
    protected void setUp() throws Exception {
        // Reset the value stack
        ValueStack stack = new OgnlValueStack();
        ActionContext.setContext(new ActionContext(stack.getContext()));

        //  clear out configuration
        configurationManager.destroyConfiguration();

        // clear out localization
        LocalizedTextUtil.reset();

        // type conversion
        XWorkConverter.resetInstance();

        // reset ognl
        OgnlValueStack.reset();
    }

    protected void tearDown() throws Exception {
        // reset the old object factory
        ObjectFactory.setObjectFactory(new ObjectFactory());

        //  clear out configuration
        configurationManager.destroyConfiguration();
    }
}