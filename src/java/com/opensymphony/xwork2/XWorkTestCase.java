/*
 * Copyright (c) 2002-2006 by OpenSymphony
 * All rights reserved.
 */

package com.opensymphony.xwork2;

import com.opensymphony.xwork2.config.Configuration;
import com.opensymphony.xwork2.config.ConfigurationManager;
import com.opensymphony.xwork2.config.impl.MockConfiguration;
import com.opensymphony.xwork2.inject.Container;
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

    protected ConfigurationManager configurationManager;
    protected Configuration configuration;
    protected Container container;
    
    protected void setUp() throws Exception {
        configurationManager = new ConfigurationManager();
        
        // Reset the value stack
        ValueStack stack = new OgnlValueStack();
        ActionContext.setContext(new ActionContext(stack.getContext()));

        // clear out localization
        LocalizedTextUtil.reset();

        // type conversion
        XWorkConverter.resetInstance();

        // reset ognl
        OgnlValueStack.reset();
        
        configuration = new MockConfiguration();
        container = configuration.getContainer();
        ObjectFactory.setObjectFactory(new ObjectFactory());
    }

    protected void tearDown() throws Exception {
        // reset the old object factory
        ObjectFactory.setObjectFactory(null);

        //  clear out configuration
        if (configurationManager != null) {
            configurationManager.destroyConfiguration();
            configurationManager = null;
        }
        configuration = null;
        container = null;
        ActionContext.setContext(null);
    }
}
